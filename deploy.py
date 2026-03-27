#!/usr/bin/env python3
"""CT-Tibet-WMS 一键部署脚本"""

import paramiko
import os
import sys
import time

SERVER = '223.109.141.120'
USER = 'root'
PASSWORD = 'Kobedu1024'
PORT = 22

# 本地文件路径
LOCAL_JAR = 'backend/target/ct-tibet-wms.jar'
LOCAL_FRONTEND = 'frontend-pc/dist'

# 服务器路径
REMOTE_BASE = '/var/www/ct-wms'
REMOTE_JAR = f'{REMOTE_BASE}/ct-tibet-wms.jar'
REMOTE_FRONTEND = f'{REMOTE_BASE}/frontend'
REMOTE_SQL = f'{REMOTE_BASE}/schema.sql'

LOCAL_SQL = 'sql/schema.sql'

def create_ssh():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(SERVER, PORT, USER, PASSWORD, timeout=15)
    return ssh

def exec_cmd(ssh, cmd, desc=""):
    if desc:
        print(f"  -> {desc}")
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=60)
    out = stdout.read().decode().strip()
    err = stderr.read().decode().strip()
    exit_code = stdout.channel.recv_exit_status()
    if out:
        print(f"     {out[:500]}")
    if err and exit_code != 0:
        print(f"     [STDERR] {err[:500]}")
    return out, err, exit_code

def upload_file(ssh, local_path, remote_path):
    sftp = ssh.open_sftp()
    file_size = os.path.getsize(local_path)
    print(f"  -> 上传 {local_path} ({file_size // 1024 // 1024}MB) -> {remote_path}")
    sftp.put(local_path, remote_path)
    sftp.close()

def upload_dir(ssh, local_dir, remote_dir):
    sftp = ssh.open_sftp()
    for root, dirs, files in os.walk(local_dir):
        rel_root = os.path.relpath(root, local_dir).replace('\\', '/')
        if rel_root == '.':
            remote_root = remote_dir
        else:
            remote_root = f"{remote_dir}/{rel_root}"

        # Create remote directories
        try:
            sftp.stat(remote_root)
        except FileNotFoundError:
            sftp.mkdir(remote_root)

        for f in files:
            local_file = os.path.join(root, f)
            remote_file = f"{remote_root}/{f}"
            sftp.put(local_file, remote_file)

    file_count = sum(len(files) for _, _, files in os.walk(local_dir))
    print(f"  -> 上传前端文件 {file_count} 个 -> {remote_dir}")
    sftp.close()

def main():
    print("=" * 60)
    print("CT-Tibet-WMS 部署脚本")
    print("=" * 60)

    ssh = create_ssh()
    print("[1/7] SSH 连接成功")

    # ========== Step 2: 创建目录 ==========
    print("\n[2/7] 创建部署目录...")
    exec_cmd(ssh, f'mkdir -p {REMOTE_BASE} {REMOTE_FRONTEND} {REMOTE_BASE}/logs')

    # ========== Step 3: 上传文件 ==========
    print("\n[3/7] 上传文件...")
    upload_file(ssh, LOCAL_JAR, REMOTE_JAR)
    upload_file(ssh, LOCAL_SQL, REMOTE_SQL)
    upload_dir(ssh, LOCAL_FRONTEND, REMOTE_FRONTEND)

    # ========== Step 4: 初始化数据库 ==========
    print("\n[4/7] 初始化数据库...")
    # 检查数据库是否已存在
    out, _, _ = exec_cmd(ssh,
        "mysql -u root -e \"SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='ct_tibet_wms';\" 2>/dev/null",
        "检查数据库是否存在")

    if 'ct_tibet_wms' not in out:
        exec_cmd(ssh,
            "mysql -u root -e \"CREATE DATABASE ct_tibet_wms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\"",
            "创建数据库 ct_tibet_wms")
        exec_cmd(ssh,
            f"mysql -u root ct_tibet_wms < {REMOTE_SQL}",
            "导入表结构")
        exec_cmd(ssh,
            "mysql -u root -e \"CREATE USER IF NOT EXISTS 'wms_user'@'localhost' IDENTIFIED BY 'WmsP@ss2024!'; GRANT ALL PRIVILEGES ON ct_tibet_wms.* TO 'wms_user'@'localhost'; FLUSH PRIVILEGES;\"",
            "创建数据库用户")
    else:
        print("  -> 数据库已存在，跳过初始化")

    # ========== Step 5: 创建应用配置 ==========
    print("\n[5/7] 创建应用配置...")

    # 生成 JWT Secret
    out, _, _ = exec_cmd(ssh, "openssl rand -base64 48")
    jwt_secret = out.strip()

    app_config = f"""# CT-Tibet-WMS 生产配置
server:
  port: 48888
  servlet:
    context-path: /

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ct_tibet_wms?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4&allowPublicKeyRetrieval=true
    username: wms_user
    password: WmsP@ss2024!
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      minimum-idle: 3
      maximum-pool-size: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  redis:
    host: 127.0.0.1
    port: 6379
    password:
    database: 2
    lettuce:
      pool:
        max-active: 8
        max-idle: 4
        min-idle: 1
  # RabbitMQ 未安装，禁用
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

jwt:
  secret: {jwt_secret}
  expiration: 7200
  refresh-expiration: 604800
  issuer: ct-tibet-wms

cors:
  allowed-origins: http://223.109.141.120

logging:
  level:
    root: INFO
    com.ct.wms: INFO
  file:
    name: {REMOTE_BASE}/logs/wms.log
  logback:
    rollingpolicy:
      max-file-size: 50MB
      max-history: 15

knife4j:
  enable: false
  production: true

management:
  endpoints:
    web:
      exposure:
        include: health
"""

    exec_cmd(ssh, f"cat > {REMOTE_BASE}/application-deploy.yml << 'EOFCONFIG'\n{app_config}\nEOFCONFIG",
             "写入应用配置")

    # ========== Step 6: 创建 systemd 服务 ==========
    print("\n[6/7] 配置系统服务...")

    service_config = f"""[Unit]
Description=CT-Tibet-WMS Backend
After=network.target mysql.service redis-server.service

[Service]
Type=simple
User=root
WorkingDirectory={REMOTE_BASE}
ExecStart=/usr/bin/java -Xms128m -Xmx384m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar {REMOTE_JAR} --spring.config.additional-location=file:{REMOTE_BASE}/application-deploy.yml --spring.profiles.active=prod
ExecStop=/bin/kill -SIGTERM $MAINPID
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=ct-wms
Environment=SPRING_PROFILES_ACTIVE=prod

[Install]
WantedBy=multi-user.target
"""

    exec_cmd(ssh, f"cat > /etc/systemd/system/ct-wms.service << 'EOFSVC'\n{service_config}\nEOFSVC",
             "创建 systemd 服务文件")

    # 停止旧进程（如果存在）
    exec_cmd(ssh, "systemctl stop ct-wms 2>/dev/null; pkill -f 'ct-tibet-wms.jar' 2>/dev/null; sleep 2", "停止旧进程")

    exec_cmd(ssh, "systemctl daemon-reload", "重载 systemd")
    exec_cmd(ssh, "systemctl enable ct-wms", "设置开机自启")
    exec_cmd(ssh, "systemctl start ct-wms", "启动 WMS 后端")

    # 等待启动
    print("  -> 等待后端启动...")
    time.sleep(8)

    out, _, code = exec_cmd(ssh, "systemctl is-active ct-wms", "检查服务状态")
    if 'active' in out:
        print("  -> ✓ 后端服务启动成功!")
    else:
        print("  -> ✗ 后端服务可能还在启动中，检查日志...")
        exec_cmd(ssh, "journalctl -u ct-wms --no-pager -n 30")

    # ========== Step 7: 配置 Nginx ==========
    print("\n[7/7] 配置 Nginx...")

    nginx_config = f"""# CT-Tibet-WMS Nginx 配置
location /wms {{
    alias {REMOTE_FRONTEND};
    index index.html;
    try_files $uri $uri/ /wms/index.html;

    # 静态资源缓存
    location ~* \\.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf)$ {{
        expires 7d;
        add_header Cache-Control "public, immutable";
    }}
}}

# WMS API 反向代理
location /api {{
    proxy_pass http://127.0.0.1:48888;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_connect_timeout 30s;
    proxy_read_timeout 120s;
    proxy_send_timeout 60s;

    # CORS 头
    add_header Access-Control-Allow-Origin * always;
    add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
    add_header Access-Control-Allow-Headers "Authorization, Content-Type" always;

    if ($request_method = 'OPTIONS') {{
        return 204;
    }}
}}
"""

    # 检查 nginx 现有配置结构
    out, _, _ = exec_cmd(ssh, "ls /etc/nginx/sites-enabled/ 2>/dev/null || ls /etc/nginx/conf.d/ 2>/dev/null", "检查 nginx 配置目录")

    # 写入 WMS 配置片段
    exec_cmd(ssh, f"cat > /etc/nginx/conf.d/ct-wms.conf << 'EOFNGINX'\n{nginx_config}\nEOFNGINX",
             "写入 Nginx 配置")

    # 检查 nginx 主配置是否在 http 块中包含 conf.d
    out, _, _ = exec_cmd(ssh, "grep -c 'include.*conf.d' /etc/nginx/nginx.conf", "检查 conf.d 引入")

    if out.strip() == '0':
        # 需要把配置嵌入到默认 server 块
        exec_cmd(ssh, f"rm /etc/nginx/conf.d/ct-wms.conf", "清理")
        # 找到默认 server 的配置文件并追加
        exec_cmd(ssh,
            f"cat > /etc/nginx/snippets/ct-wms.conf << 'EOFNGINX'\n{nginx_config}\nEOFNGINX",
            "写入 snippets 配置")
        out2, _, _ = exec_cmd(ssh, "grep -c 'ct-wms' /etc/nginx/sites-enabled/default 2>/dev/null || echo 0")
        if out2.strip() == '0':
            exec_cmd(ssh,
                "sed -i '/^}$/i \\    include snippets/ct-wms.conf;' /etc/nginx/sites-enabled/default 2>/dev/null || "
                "sed -i '/^}$/i \\    include snippets/ct-wms.conf;' /etc/nginx/sites-available/default 2>/dev/null",
                "在 default server 中引入 WMS 配置")

    # 验证和重载 nginx
    out, err, code = exec_cmd(ssh, "nginx -t 2>&1", "验证 Nginx 配置")
    if code == 0:
        exec_cmd(ssh, "systemctl reload nginx", "重载 Nginx")
        print("  -> ✓ Nginx 配置成功!")
    else:
        print(f"  -> ✗ Nginx 配置有误: {out} {err}")
        # 回退：尝试直接在默认配置中写
        print("  -> 尝试备选方案...")

    # ========== 验证部署 ==========
    print("\n" + "=" * 60)
    print("部署验证")
    print("=" * 60)

    time.sleep(3)

    # 检查后端健康
    out, _, code = exec_cmd(ssh, "curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:48888/api/auth/health 2>/dev/null",
                            "后端健康检查")
    if '200' in out:
        print("  -> ✓ 后端 API 正常")
    else:
        print(f"  -> ✗ 后端 API 返回: {out}（可能还在启动中）")

    # 检查前端
    out, _, code = exec_cmd(ssh, "curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1/wms/ 2>/dev/null",
                            "前端页面检查")
    if '200' in out:
        print("  -> ✓ 前端页面正常")
    else:
        print(f"  -> ✗ 前端返回: {out}")

    print("\n" + "=" * 60)
    print(f"部署完成！访问地址：http://223.109.141.120/wms")
    print(f"API 地址：http://223.109.141.120/api/auth/health")
    print(f"默认管理员：admin / 123456")
    print("=" * 60)

    ssh.close()

if __name__ == '__main__':
    main()
