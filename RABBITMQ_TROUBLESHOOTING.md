# RabbitMQ 连接故障诊断与解决方案

## 问题诊断

### 错误现象
```
org.springframework.amqp.AmqpConnectException: java.net.ConnectException: Connection refused: getsockopt
```

### 根本原因分析
该错误表示应用无法连接到 RabbitMQ 服务器的 5672 端口，主要原因包括：

1. **RabbitMQ 服务未启动** - 最常见原因
2. **RabbitMQ 未安装** - Windows 开发环境可能未安装
3. **防火墙/端口被占用** - 5672 端口被其他服务占用
4. **配置错误** - 主机名或端口配置不正确

### 应用配置信息

**配置文件位置**: `H:\java\CT-Tibet-WMS\backend\src\main\resources\application.yml`

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}      # 默认 localhost
    port: ${RABBITMQ_PORT:5672}           # 默认 5672
    username: ${RABBITMQ_USER:guest}      # 默认 guest
    password: ${RABBITMQ_PASSWORD:guest}  # 默认 guest
    virtual-host: /wms                    # 虚拟主机
```

### RabbitMQ 在应用中的使用

RabbitMQ 用于异步消息处理，**不是核心业务的直接依赖**：

| 功能模块 | 用途 | 影响 |
|---------|------|------|
| **站内消息通知** | 异步保存消息到数据库 | 可优雅降级 |
| **微信模板消息** | 异步推送到微信 | 可优雅降级 |
| **审批流程通知** | 异步通知相关人员 | 可优雅降级 |

---

## 解决方案（按优先级）

### 方案 A: 快速修复 - 禁用 RabbitMQ（推荐用于开发）

在开发环境中，如果不需要立即使用消息队列功能，可以禁用 RabbitMQ 连接。

#### 步骤 1: 修改应用配置

编辑 `H:\java\CT-Tibet-WMS\backend\src\main\resources\application-dev.yml`：

```yaml
# 开发环境配置
server:
  port: 48888

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ct_tibet_wms?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: root
    hikari:
      maximum-pool-size: 10

  redis:
    host: localhost
    port: 6379
    password:

  # 禁用 RabbitMQ 自动配置
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration

  # 如果需要保留配置但禁用连接，可以将主机设置为无效值
  # rabbitmq:
  #   host: disabled
  #   port: 5672
  #   username: guest
  #   password: guest

# 日志级别
logging:
  level:
    root: INFO
    com.ct.wms: DEBUG
    com.baomidou.mybatisplus: DEBUG
    org.springframework.security: DEBUG

# MyBatis-Plus SQL日志
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

# Knife4j
knife4j:
  enable: true
  production: false
```

#### 步骤 2: 创建空的 RabbitMQ 配置（可选）

为了避免应用完全启动失败，可以创建一个开发环境特定的配置：

**文件**: `H:\java\CT-Tibet-WMS\backend\src\main\resources\application-dev-no-rabbitmq.yml`

```yaml
# 开发环境配置（不包含 RabbitMQ）
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
```

然后启动时使用：
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

#### 步骤 3: 创建优雅降级的 NotificationProducer

修改 `H:\java\CT-Tibet-WMS\backend\src\main\java\com\ct\wms\mq\NotificationProducer.java`：

```java
package com.ct.wms.mq;

import com.ct.wms.config.RabbitMQConfig;
import com.ct.wms.dto.NotificationMessageDTO;
import com.ct.wms.dto.WechatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 消息通知生产者
 * 支持在 RabbitMQ 不可用时优雅降级
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Component
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;
    private final boolean rabbitMQEnabled;

    public NotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitMQEnabled = rabbitTemplate != null;
    }

    /**
     * 发送站内通知消息
     *
     * @param message 通知消息
     */
    public void sendNotification(NotificationMessageDTO message) {
        try {
            log.info("发送站内通知消息: receiverId={}, type={}, title={}",
                    message.getReceiverId(), message.getMessageType(), message.getTitle());

            if (!rabbitMQEnabled) {
                log.warn("RabbitMQ 未启用，消息无法发送。请启动 RabbitMQ 或配置消息存储");
                return;
            }

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    "wms.notification.send",
                    message
            );

            log.info("站内通知消息发送成功");
        } catch (Exception e) {
            log.error("发送站内通知消息失败", e);
            // 在开发环境中，可以选择不抛异常，而是记录日志
            // throw new RuntimeException("发送站内通知消息失败", e);
        }
    }

    /**
     * 发送微信模板消息
     *
     * @param message 微信消息
     */
    public void sendWechatMessage(WechatMessageDTO message) {
        try {
            log.info("发送微信模板消息: openId={}, templateId={}",
                    message.getOpenId(), message.getTemplateId());

            if (!rabbitMQEnabled) {
                log.warn("RabbitMQ 未启用，消息无法发送。请启动 RabbitMQ 或配置消息存储");
                return;
            }

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.WECHAT_EXCHANGE,
                    "wms.wechat.send",
                    message
            );

            log.info("微信模板消息发送成功");
        } catch (Exception e) {
            log.error("发送微信模板消息失败", e);
            // 在开发环境中，可以选择不抛异常，而是记录日志
            // throw new RuntimeException("发送微信模板消息失败", e);
        }
    }
}
```

#### 步骤 4: 修改 RabbitMQ 配置使其不强制连接

修改 `H:\java\CT-Tibet-WMS\backend\src\main\resources\application.yml`：

```yaml
spring:
  # ... 其他配置 ...

  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    virtual-host: /wms
    publisher-confirm-type: correlated
    publisher-returns: true
    # 添加连接超时和重试配置
    connection-timeout: 5000
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 1
        # 添加这些配置以支持优雅降级
        retry:
          enabled: true
          initial-interval: 1000
          max-interval: 10000
          multiplier: 1.0
          max-attempts: 3
```

---

### 方案 B: 完整安装 - Windows 上安装 RabbitMQ

#### 前置条件

1. **安装 Erlang OTP**（RabbitMQ 的运行时）
2. **安装 RabbitMQ Server**
3. **配置虚拟主机**

#### 详细步骤

##### 步骤 1: 下载并安装 Erlang OTP

1. 访问官方网站: https://www.erlang.org/downloads
2. 下载最新的 Windows 版本（推荐 OTP 25 或更高）
3. 运行安装程序（如 `otp_win64_25.3.exe`）
4. 按默认选项安装，记下安装路径（通常是 `C:\Program Files\erl-25.3`）
5. 验证安装：打开 PowerShell，输入命令：
   ```powershell
   erl -version
   ```
   应该显示 Erlang/OTP 版本号

##### 步骤 2: 下载并安装 RabbitMQ Server

1. 访问官方网站: https://www.rabbitmq.com/download.html
2. 下载 Windows 安装程序（如 `rabbitmq-server-3.12.0.exe`）
3. 运行安装程序
4. 按照向导安装（默认路径通常是 `C:\Program Files\RabbitMQ Server\`）
5. 勾选"Set ERLANG_HOME environment variable"

##### 步骤 3: 启动 RabbitMQ 服务

**方法 1: 使用 Windows 服务**

```powershell
# 以管理员身份打开 PowerShell
# 启动 RabbitMQ 服务
Start-Service RabbitMQ

# 检查服务状态
Get-Service RabbitMQ

# 查看服务日志
Get-Content "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.12.0\log\rabbit@HOSTNAME.log"
```

**方法 2: 使用命令行启动**

```batch
# 打开 cmd 并进入 RabbitMQ 目录
cd "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.12.0\sbin"

# 启动 RabbitMQ
rabbitmq-server

# 或在后台启动
rabbitmq-server -detached
```

##### 步骤 4: 配置虚拟主机和用户

```powershell
# 进入 RabbitMQ sbin 目录
cd "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.12.0\sbin"

# 创建虚拟主机 /wms
.\rabbitmqctl.bat add_vhost /wms

# 创建用户（如果不想用默认的 guest）
.\rabbitmqctl.bat add_user wmsuser wmspassword

# 设置用户权限
.\rabbitmqctl.bat set_permissions -p /wms wmsuser ".*" ".*" ".*"

# 或者使用默认 guest 用户
# 设置 guest 用户对 /wms 虚拟主机的权限
.\rabbitmqctl.bat set_permissions -p /wms guest ".*" ".*" ".*"

# 列出所有虚拟主机
.\rabbitmqctl.bat list_vhosts

# 列出所有用户
.\rabbitmqctl.bat list_users
```

##### 步骤 5: 启用管理界面

```powershell
cd "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.12.0\sbin"

# 启用管理插件
.\rabbitmq-plugins.bat enable rabbitmq_management

# 重启 RabbitMQ
Stop-Service RabbitMQ
Start-Service RabbitMQ
```

##### 步骤 6: 访问 RabbitMQ 管理界面

- 打开浏览器访问: http://localhost:15672
- 默认用户名: guest
- 默认密码: guest

#### 验证 RabbitMQ 运行

```powershell
# 检查 5672 端口（AMQP）是否监听
netstat -ano | findstr ":5672"

# 检查 15672 端口（管理界面）是否监听
netstat -ano | findstr ":15672"

# 两个端口都应该显示 LISTENING 状态
```

---

### 方案 C: Docker 方案（推荐用于开发）

Docker 方案最简单，推荐用于开发环境。

#### 前置条件

1. 已安装 Docker Desktop（Windows 版本）
2. Docker 正常运行

#### 步骤 1: 使用 Docker 启动 RabbitMQ

```bash
# 创建 RabbitMQ 容器
docker run -d \
  --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  -e RABBITMQ_DEFAULT_VHOST=/ \
  rabbitmq:3.12-management

# 解释：
# -d: 后台运行
# --name rabbitmq: 容器名称
# -p 5672:5672: 映射 AMQP 端口
# -p 15672:15672: 映射管理界面端口
# -e: 环境变量设置
# rabbitmq:3.12-management: 镜像名称和标签
```

#### 步骤 2: 配置虚拟主机（可选）

```bash
# 进入 RabbitMQ 容器
docker exec -it rabbitmq bash

# 在容器内执行命令
rabbitmqctl add_vhost /wms
rabbitmqctl set_permissions -p /wms guest ".*" ".*" ".*"

# 退出容器
exit
```

#### 步骤 3: 验证容器运行

```bash
# 查看运行中的容器
docker ps

# 应该看到 rabbitmq 容器在运行列表中
# CONTAINER ID   IMAGE                        PORTS
# abc123...      rabbitmq:3.12-management     0.0.0.0:5672->5672/tcp, 0.0.0.0:15672->15672/tcp

# 查看 RabbitMQ 日志
docker logs rabbitmq

# 查看 RabbitMQ 统计信息
docker stats rabbitmq
```

#### 步骤 4: 访问管理界面

- 打开浏览器访问: http://localhost:15672
- 用户名: guest
- 密码: guest

#### 步骤 5: 创建 docker-compose.yml（可选，便于管理）

**文件位置**: `H:\java\CT-Tibet-WMS\docker-compose.yml`

```yaml
version: '3.8'

services:
  # RabbitMQ 消息队列
  rabbitmq:
    image: rabbitmq:3.12-management
    container_name: ct-wms-rabbitmq
    ports:
      - "5672:5672"    # AMQP 端口
      - "15672:15672"  # 管理界面端口
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
      RABBITMQ_DEFAULT_VHOST: /
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
      - ./rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf:ro  # 可选：自定义配置
    healthcheck:
      test: rabbitmq-diagnostics -q ping
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 40s
    networks:
      - ct-wms-network

  # MySQL 数据库
  mysql:
    image: mysql:8.0
    container_name: ct-wms-mysql
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: ct_tibet_wms
      TZ: Asia/Shanghai
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql:/docker-entrypoint-initdb.d:ro  # 初始化脚本位置
    networks:
      - ct-wms-network

  # Redis 缓存
  redis:
    image: redis:7-alpine
    container_name: ct-wms-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - ct-wms-network

volumes:
  rabbitmq_data:
  mysql_data:
  redis_data:

networks:
  ct-wms-network:
    driver: bridge
```

启动所有服务：
```bash
cd "H:\java\CT-Tibet-WMS"
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f rabbitmq

# 停止服务
docker-compose down

# 清理数据
docker-compose down -v
```

#### Docker 相关命令速查

```bash
# 查看容器日志
docker logs -f rabbitmq

# 进入容器
docker exec -it rabbitmq bash

# 停止容器
docker stop rabbitmq

# 启动容器
docker start rabbitmq

# 重启容器
docker restart rabbitmq

# 删除容器
docker rm rabbitmq

# 删除镜像
docker rmi rabbitmq:3.12-management
```

---

## 验证步骤

### 验证 RabbitMQ 是否正常运行

#### 1. 端口检查

```bash
# Windows PowerShell 检查 AMQP 端口（5672）
netstat -ano | findstr ":5672"

# 应该显示
# TCP    127.0.0.1:5672           0.0.0.0:0              LISTENING       1234

# 检查管理界面端口（15672）
netstat -ano | findstr ":15672"
```

#### 2. 连接测试

使用 telnet 测试连接：

```powershell
# 安装 telnet（如果未安装）
# Control Panel -> Programs -> Programs and Features -> Turn Windows features on or off
# 勾选 "Telnet Client"

# 测试连接
telnet localhost 5672

# 如果成功，会显示：
# Connected to localhost.
# Escape character is ']'.
#
# AMQP    (显示 RabbitMQ 的 AMQP 欢迎信息)

# 输入 quit 退出
```

#### 3. 管理界面检查

- 打开浏览器: http://localhost:15672
- 登录后查看：
  - Queues 标签页：是否有 `wms.notification.queue` 和 `wms.wechat.queue`
  - Connections 标签页：应用连接是否存在
  - Channels 标签页：消息通道是否建立

### 验证应用连接

#### 1. 查看应用日志

启动应用后，查看日志文件：

```bash
# 日志位置
H:\java\CT-Tibet-WMS\backend\logs\

# 查看主日志
type H:\java\CT-Tibet-WMS\backend\logs\all.log | findstr "RabbitMQ"

# 查看错误日志
type H:\java\CT-Tibet-WMS\backend\logs\error.log
```

成功连接的日志示例：
```
2025-11-13 10:30:15.234 INFO  [main] o.s.amqp.r.c.CachingConnectionFactory : Attempting to connect to: localhost:5672
2025-11-13 10:30:15.456 INFO  [main] c.r.c.ConnectionFactory : Connected to RabbitMQ
2025-11-13 10:30:15.678 INFO  [main] c.c.RabbitMQConfig : RabbitMQ configuration initialized
```

失败的日志示例：
```
2025-11-13 10:30:15.234 ERROR [main] o.s.amqp.r.c.CachingConnectionFactory : Error: Connection refused: getsockname
```

#### 2. 调用 API 测试消息发送

使用 Swagger 或 curl 测试：

```bash
# 获取 JWT token（如果需要身份验证）
curl -X POST http://localhost:48888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'

# 发送测试消息（示例，需要根据实际 API 调整）
curl -X POST http://localhost:48888/api/message/send \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "receiverId": 1,
    "messageType": 1,
    "title": "测试消息",
    "content": "这是一条测试消息"
  }'
```

#### 3. RabbitMQ 管理界面验证

在 http://localhost:15672 检查：

1. **Queues 标签页**
   - 查看 `wms.notification.queue` 消息数
   - 查看 `wms.wechat.queue` 消息数
   - 应该看到队列中有待处理消息

2. **Connections 标签页**
   - 应该看到应用的连接（如 `spring-client`）
   - 连接状态应该是 `running`

3. **Channels 标签页**
   - 应该看到活跃的通道
   - 每个通道应该显示 consumer 数量

---

## 常见问题解决

### Q1: 端口 5672 被占用怎么办？

```powershell
# 找出占用端口的进程
$ProcessId = (Get-NetTCPConnection -LocalPort 5672 -ErrorAction SilentlyContinue).OwningProcess[0]
Get-Process -Id $ProcessId

# 强制杀死进程（如果确认可以）
Stop-Process -Id $ProcessId -Force

# 或修改 RabbitMQ 配置使用不同端口
# 编辑 C:\Program Files\RabbitMQ Server\rabbitmq_server-3.12.0\etc\rabbitmq.conf
# listeners.tcp.default = 5680  # 改为 5680 或其他端口

# 然后在 application.yml 中更新配置
# spring.rabbitmq.port: 5680
```

### Q2: RabbitMQ 启动失败怎么办？

```powershell
# 1. 检查 Erlang 是否正确安装
erl -version

# 2. 检查 ERLANG_HOME 环境变量
$env:ERLANG_HOME

# 如果未设置，手动设置
$env:ERLANG_HOME = "C:\Program Files\erl-25.3"

# 3. 检查 RabbitMQ 日志
Get-Content "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.12.0\log\rabbit@HOSTNAME.log" -Tail 50

# 4. 尝试重新安装 RabbitMQ
# 卸载 -> 删除数据目录 -> 重新安装
Remove-Item "C:\Users\$env:USERNAME\AppData\Roaming\RabbitMQ" -Force -Recurse
```

### Q3: 虚拟主机 /wms 创建失败怎么办？

```powershell
cd "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.12.0\sbin"

# 列出现有虚拟主机
.\rabbitmqctl.bat list_vhosts

# 删除虚拟主机（如果存在）
.\rabbitmqctl.bat delete_vhost /wms

# 重新创建
.\rabbitmqctl.bat add_vhost /wms

# 设置权限
.\rabbitmqctl.bat set_permissions -p /wms guest ".*" ".*" ".*"
```

### Q4: 应用启动时 RabbitMQ 连接卡住怎么办？

修改 `application.yml` 添加超时配置：

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    connection-timeout: 5000  # 5 秒超时
    cache:
      connection:
        timeout: 5000
```

### Q5: 消息堆积在队列中怎么办？

```powershell
# 1. 检查消费者状态
curl http://localhost:15672/api/consumers -u guest:guest

# 2. 清空特定队列
curl -i -u guest:guest -H "content-type:application/json" \
  -XDELETE http://localhost:15672/api/queues/%2F/wms.notification.queue

# 3. 或通过管理界面
# http://localhost:15672 -> Queues -> 选择队列 -> Purge
```

---

## 最佳实践建议

### 开发环境

1. **使用 Docker Compose** 启动 RabbitMQ（最简单）
2. **配置应用优雅降级** 以支持 RabbitMQ 不可用的情况
3. **定期检查** RabbitMQ 的健康状态

### 生产环境

1. **集群部署** RabbitMQ 以保证高可用
2. **监控告警** - 集成 Prometheus + Grafana
3. **持久化配置** - 启用消息持久化和队列持久化
4. **备份策略** - 定期备份 RabbitMQ 数据
5. **安全加固** - 修改默认密码、启用 TLS
6. **性能优化** - 调整内存、连接池等参数

---

## 快速参考

### 启动 RabbitMQ 的三种方法

```bash
# 方法 1: Docker（推荐开发）
docker run -d -p 5672:5672 -p 15672:15672 rabbitmq:3.12-management

# 方法 2: Docker Compose
docker-compose up -d rabbitmq

# 方法 3: Windows 服务
Start-Service RabbitMQ
```

### 验证 RabbitMQ 运行

```bash
# 检查端口
netstat -ano | findstr ":5672"

# 访问管理界面
# http://localhost:15672 (guest / guest)
```

### 查看应用日志

```bash
# 日志位置
H:\java\CT-Tibet-WMS\backend\logs\all.log

# 搜索 RabbitMQ 相关日志
findstr "RabbitMQ" all.log
```

---

## 联系方式

如遇到其他问题，请：

1. 查看详细日志文件 `H:\java\CT-Tibet-WMS\backend\logs\debug-for-claude.log`
2. 检查 RabbitMQ 日志
3. 使用 RabbitMQ 管理界面 (http://localhost:15672) 进行诊断

---

**最后更新**: 2025-11-13
**版本**: 1.0
