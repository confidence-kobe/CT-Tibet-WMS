# CT-Tibet-WMS 安全事件响应计划

**版本**: v1.0.0
**生效日期**: 2025-11-24
**最后更新**: 2025-11-24
**负责人**: 安全团队

---

## 目录

1. [概述](#1-概述)
2. [事件分类](#2-事件分类)
3. [响应团队](#3-响应团队)
4. [响应流程](#4-响应流程)
5. [事件场景与处置](#5-事件场景与处置)
6. [通讯与上报](#6-通讯与上报)
7. [事后总结](#7-事后总结)
8. [联系方式](#8-联系方式)

---

## 1. 概述

### 1.1 目的

本计划旨在为CT-Tibet-WMS系统建立标准化的安全事件响应流程,确保在发生安全事件时能够:
- **快速响应**: 在第一时间发现和确认事件
- **有效控制**: 限制事件影响范围和损失程度
- **及时恢复**: 尽快恢复业务正常运行
- **总结改进**: 从事件中学习并改进安全措施

### 1.2 适用范围

本计划适用于以下安全事件:
- 系统入侵和数据泄露
- 恶意代码和病毒感染
- 拒绝服务攻击 (DoS/DDoS)
- 未授权访问和权限滥用
- 数据篡改和完整性破坏
- 配置错误导致的安全问题
- 第三方供应链安全事件

### 1.3 响应原则

- **快速**: 15分钟内响应,1小时内初步分析
- **专业**: 由专业安全人员主导处置
- **协同**: 跨部门协作,统一指挥
- **保密**: 事件信息严格保密,防止二次伤害
- **合规**: 符合法律法规和行业标准要求

---

## 2. 事件分类

### 2.1 严重程度分级

| 级别 | 名称 | 描述 | 响应时间 | 示例 |
|------|------|------|----------|------|
| P0 | 紧急 (Critical) | 影响核心业务,造成重大损失 | 立即响应 (15分钟内) | 数据库被删除、大规模数据泄露、系统全面瘫痪 |
| P1 | 高危 (High) | 影响重要功能,可能造成损失 | 1小时内响应 | 管理员账户被盗、敏感数据泄露、持续攻击 |
| P2 | 中危 (Medium) | 影响部分功能,损失有限 | 4小时内响应 | 普通账户被盗、小规模攻击、配置错误 |
| P3 | 低危 (Low) | 影响轻微,无明显损失 | 24小时内响应 | 安全扫描异常、可疑行为、潜在威胁 |

### 2.2 事件类型

**1. 入侵事件**
- 服务器被入侵
- 数据库被攻击
- 容器逃逸

**2. 数据安全事件**
- 数据泄露
- 数据篡改
- 数据丢失

**3. 账户安全事件**
- 账户被盗
- 权限滥用
- 批量异常登录

**4. 可用性事件**
- DDoS攻击
- 资源耗尽
- 服务中断

**5. 合规事件**
- 审计异常
- 违规操作
- 合规检查失败

---

## 3. 响应团队

### 3.1 团队架构

```
安全事件响应指挥部
├── 总指挥 (CTO/安全负责人)
├── 技术响应组
│   ├── 安全工程师 (主导)
│   ├── 后端开发
│   ├── 运维工程师
│   └── 数据库管理员
├── 业务协调组
│   ├── 产品经理
│   ├── 客服负责人
│   └── 业务负责人
└── 法务公关组
    ├── 法务顾问
    ├── 公关负责人
    └── 客户关系经理
```

### 3.2 角色职责

**总指挥**:
- 全面负责事件响应工作
- 决策重大事项 (如系统下线、报警等)
- 协调资源和人员
- 对外沟通

**技术响应组**:
- 事件分析和调查
- 漏洞修复和系统加固
- 数据恢复
- 技术文档编写

**业务协调组**:
- 评估业务影响
- 协调业务部门
- 用户沟通
- 损失统计

**法务公关组**:
- 法律风险评估
- 监管部门沟通
- 媒体应对
- 合规处理

---

## 4. 响应流程

### 4.1 六阶段响应模型

```
检测 → 确认 → 遏制 → 根除 → 恢复 → 总结
  ↓       ↓       ↓       ↓       ↓       ↓
监控    分析    隔离    修复    验证    改进
```

---

### 4.2 阶段1: 检测 (Detection)

**目标**: 及时发现安全事件

**检测来源**:
- 安全监控系统告警
- 用户投诉反馈
- 安全扫描发现
- 内部审计发现
- 外部通报

**检测工具**:
```bash
# 实时日志监控
tail -f /app/logs/ct-wms.log | grep -E "ERROR|WARN|UNAUTHORIZED|FAILED"

# 异常登录检测
grep "Login failed" /app/logs/security.log | awk '{print $5}' | sort | uniq -c | sort -rn

# 可疑请求检测
tail -f /var/log/nginx/access.log | grep -E "\.\.\/|<script|union.*select"

# 系统资源监控
top -b -n 1 | grep "Cpu\|Mem"
```

**告警阈值**:
- 登录失败次数 > 10次/分钟
- API错误率 > 5%
- 响应时间 > 5秒
- 数据库连接数 > 80%
- CPU/内存使用率 > 90%

**行动**:
1. ✅ 记录事件发现时间和来源
2. ✅ 收集初步证据 (日志、截图)
3. ✅ 通知安全团队
4. ✅ 创建事件工单

---

### 4.3 阶段2: 确认 (Identification)

**目标**: 快速判断是否为真实安全事件

**确认步骤**:

**1. 快速分析** (15分钟内):
```bash
# 检查最近的登录记录
SELECT * FROM tb_user_login_log
WHERE create_time > DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY create_time DESC LIMIT 50;

# 检查异常操作
SELECT * FROM tb_audit_log
WHERE operation IN ('DELETE', 'UPDATE_PERMISSION', 'RESET_PASSWORD')
AND create_time > DATE_SUB(NOW(), INTERVAL 1 HOUR);

# 检查系统进程
ps aux | grep -E "java|mysql|redis|nginx"

# 检查网络连接
netstat -antp | grep ESTABLISHED
```

**2. 事件分类**:
- 真实安全事件 → 进入下一阶段
- 误报 → 优化检测规则,关闭工单
- 测试行为 → 记录并关闭
- 待进一步观察 → 持续监控

**3. 初步评估**:
- 影响范围 (用户数、数据量、系统数)
- 严重程度 (P0/P1/P2/P3)
- 是否正在进行攻击
- 是否已造成损失

**4. 启动响应**:
- P0/P1: 立即召集响应团队
- P2: 通知相关人员
- P3: 按计划处理

**决策点**:
- **是否为安全事件?** Yes → 继续 / No → 关闭工单
- **严重程度?** P0/P1 → 紧急响应 / P2/P3 → 常规处理
- **是否需要隔离?** Yes → 进入遏制阶段

---

### 4.4 阶段3: 遏制 (Containment)

**目标**: 防止事件扩大,限制损失

**短期遏制** (立即执行):

**1. 账户安全**:
```bash
# 冻结可疑账户
UPDATE tb_user SET status = 1 WHERE id = {suspicious_user_id};

# 撤销可疑Token
redis-cli SET "token:blacklist:{token}" "1" EX 7200

# 强制所有用户重新登录 (极端情况)
redis-cli FLUSHDB  # 清空Redis (慎用!)
```

**2. 网络隔离**:
```bash
# 封禁攻击IP (iptables)
iptables -A INPUT -s {attack_ip} -j DROP

# Nginx封禁
location / {
    deny {attack_ip};
}

# CloudFlare封禁 (如使用)
curl -X POST "https://api.cloudflare.com/client/v4/zones/{zone_id}/firewall/access_rules/rules" \
  -H "Authorization: Bearer {api_token}" \
  -d '{"mode":"block","configuration":{"target":"ip","value":"{attack_ip}"}}'
```

**3. 服务降级**:
```bash
# 关闭非核心功能
kubectl scale deployment statistics-service --replicas=0

# 启用维护模式
touch /app/maintenance.flag

# 限制访问速率 (Nginx)
limit_req zone=api_limit burst=5 nodelay;
```

**4. 数据保护**:
```bash
# 立即备份当前数据
mysqldump -u root -p ct_tibet_wms > /backup/emergency_$(date +%Y%m%d_%H%M%S).sql

# 启用只读模式 (MySQL)
SET GLOBAL read_only = ON;

# 备份关键日志
tar -czf /backup/logs_$(date +%Y%m%d_%H%M%S).tar.gz /app/logs/
```

**长期遏制** (1-4小时内):

**1. 系统加固**:
- 修改所有管理员密码
- 更换JWT密钥
- 更新防火墙规则
- 关闭不必要的端口

**2. 证据保全**:
- 保存所有相关日志
- 保存内存快照
- 保存网络流量包
- 拍照记录

**决策点**:
- **是否需要下线系统?** Yes → 通知用户并下线
- **是否需要报警?** Yes → 联系公安部门
- **损失是否可控?** Yes → 继续 / No → 重新评估

---

### 4.5 阶段4: 根除 (Eradication)

**目标**: 彻底消除威胁根源

**根除步骤**:

**1. 漏洞修复**:
```java
// 示例: 修复SQL注入漏洞
// Before (vulnerable)
String sql = "SELECT * FROM tb_user WHERE username = '" + username + "'";

// After (fixed)
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getUsername, username);
List<User> users = userMapper.selectList(wrapper);
```

**2. 恶意代码清除**:
```bash
# 查找可疑文件 (最近24小时修改)
find /app -type f -mtime -1 -name "*.jsp" -o -name "*.sh"

# 查找Webshell特征
grep -r "eval(" /app/
grep -r "exec(" /app/

# 删除恶意文件
rm -f /app/malicious_file.jsp
```

**3. 后门清除**:
```bash
# 检查定时任务
crontab -l

# 检查启动项
systemctl list-unit-files | grep enabled

# 检查SSH授权
cat ~/.ssh/authorized_keys
```

**4. 配置修复**:
```yaml
# 修复弱配置
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/ct_tibet_wms?useSSL=true  # 启用SSL
    username: wms_user  # 非root账户
    password: ${DB_PASSWORD}  # 强密码
```

**5. 依赖更新**:
```bash
# 更新有漏洞的依赖
mvn versions:use-latest-releases

# 重新构建镜像
docker build -t ct-tibet-wms:latest .
```

**验证**:
- 再次扫描漏洞
- 尝试复现攻击
- 安全测试通过

---

### 4.6 阶段5: 恢复 (Recovery)

**目标**: 安全地恢复业务运行

**恢复步骤**:

**1. 数据恢复** (如有损失):
```bash
# 从备份恢复数据库
mysql -u root -p ct_tibet_wms < /backup/pre_incident.sql

# 验证数据完整性
SELECT COUNT(*) FROM tb_user;
SELECT COUNT(*) FROM tb_apply;
SELECT COUNT(*) FROM tb_outbound;
```

**2. 系统验证**:
```bash
# 健康检查
curl http://localhost:48888/actuator/health

# 功能测试
./run-tests.sh

# 性能测试
ab -n 1000 -c 10 http://localhost:48888/api/users
```

**3. 监控加强**:
```bash
# 部署额外监控
kubectl apply -f monitoring/enhanced-monitoring.yaml

# 查看实时日志
tail -f /app/logs/*.log | grep -E "ERROR|WARN"

# 监控攻击IP
iptables -L -n -v | grep DROP
```

**4. 分阶段恢复**:
```
阶段1 (0-2小时):   核心功能恢复 (登录、查询)
阶段2 (2-6小时):   重要功能恢复 (申请、审批)
阶段3 (6-24小时):  全部功能恢复 (统计、报表)
阶段4 (24小时+):   持续监控观察
```

**5. 用户通知**:
```
尊敬的用户:

我们检测到系统存在安全风险,已紧急修复。现系统已恢复正常,您的数据安全。

出于安全考虑,请您:
1. 修改密码
2. 检查账户活动记录
3. 如有异常请联系我们

感谢您的理解与支持。

西藏电信WMS团队
2025-11-24
```

**决策点**:
- **系统是否稳定?** Yes → 逐步放开限制
- **数据是否完整?** Yes → 正式恢复
- **是否需要持续观察?** Yes → 7x24小时监控

---

### 4.7 阶段6: 总结 (Lessons Learned)

**目标**: 从事件中学习,防止再次发生

**事后分析会** (72小时内):

**参会人员**:
- 事件响应团队全员
- 技术负责人
- 业务负责人
- 安全负责人

**讨论议题**:
1. 事件时间线回顾
2. 响应过程评估
3. 做得好的地方
4. 需要改进的地方
5. 根本原因分析
6. 预防措施

**输出文档**:

**事件报告模板**:
```markdown
# 安全事件报告

## 1. 事件概述
- 事件编号: INC-20251124-001
- 严重程度: P1 - 高危
- 事件类型: 数据泄露
- 发生时间: 2025-11-24 14:30
- 发现时间: 2025-11-24 14:35
- 恢复时间: 2025-11-24 18:20
- 影响范围: 50个用户账户

## 2. 事件经过

### 时间线
- 14:30 - 攻击者通过SQL注入获取数据库访问权限
- 14:32 - 攻击者导出用户表数据
- 14:35 - 监控系统发现异常数据库查询
- 14:40 - 安全团队确认事件
- 14:45 - 封禁攻击IP,修改数据库密码
- 15:00 - 修复SQL注入漏洞
- 16:00 - 完成代码部署
- 17:00 - 功能测试通过
- 18:00 - 逐步恢复服务
- 18:20 - 系统完全恢复

### 攻击手法
攻击者利用/api/users接口的keyword参数SQL注入漏洞...

## 3. 影响评估
- 受影响用户: 50人
- 泄露数据: 用户名、手机号(部分)、邮箱
- 业务损失: 约4小时服务中断
- 经济损失: 待评估

## 4. 根本原因
1. 代码审查不严格,存在SQL注入漏洞
2. WAF未部署,无法拦截攻击
3. 监控告警不及时
4. 数据库使用root账户

## 5. 改进措施

### 立即行动 (1周内)
- [ ] 全面代码审计,修复类似漏洞
- [ ] 部署WAF
- [ ] 数据库使用专用账户
- [ ] 加强监控告警

### 短期改进 (1个月内)
- [ ] 实施SAST/DAST自动化扫描
- [ ] 加强代码审查流程
- [ ] 安全培训

### 长期改进 (持续)
- [ ] 建立漏洞悬赏计划
- [ ] 定期渗透测试
- [ ] 安全文化建设

## 6. 责任认定
根据公司安全管理制度...

## 7. 附件
- 详细日志
- 攻击流量包
- 修复代码Commit
```

---

## 5. 事件场景与处置

### 场景1: 数据库被删除 (P0)

**症状**:
- 用户无法登录
- 所有API返回数据库错误
- 监控显示数据库连接失败

**立即行动** (15分钟内):
```bash
# 1. 确认数据库状态
mysql -u root -p -e "SHOW DATABASES;"

# 2. 检查是否被删除
if ! mysql -u root -p ct_tibet_wms -e "SELECT 1"; then
    echo "数据库已被删除!"
fi

# 3. 停止应用防止进一步损坏
systemctl stop ct-wms

# 4. 立即从最新备份恢复
mysql -u root -p < /backup/ct_tibet_wms_latest.sql

# 5. 验证数据
mysql -u root -p ct_tibet_wms -e "SELECT COUNT(*) FROM tb_user;"

# 6. 重启应用
systemctl start ct-wms
```

**后续处理**:
- 分析审计日志,找出攻击来源
- 修改所有数据库密码
- 加强数据库访问控制
- 启用数据库审计日志

---

### 场景2: 管理员账户被盗 (P1)

**症状**:
- 异地登录告警
- 异常操作 (批量删除用户、修改权限)
- 用户投诉无法登录

**立即行动** (30分钟内):
```sql
-- 1. 冻结被盗账户
UPDATE tb_user SET status = 1 WHERE id = {admin_user_id};

-- 2. 撤销所有Token
DELETE FROM tb_user_token WHERE user_id = {admin_user_id};

-- 3. 回滚恶意操作 (如有)
-- 从审计日志中找到恶意操作
SELECT * FROM tb_audit_log
WHERE user_id = {admin_user_id}
AND create_time > '2025-11-24 14:00:00'
ORDER BY create_time DESC;

-- 恢复被删除的用户
UPDATE tb_user SET deleted = 0
WHERE id IN (/* 被删除的用户ID列表 */);

-- 4. 重置密码
-- 线下联系真实管理员,设置新密码

-- 5. 启用多因素认证
UPDATE tb_user SET mfa_enabled = 1 WHERE role_id = 1;
```

**后续处理**:
- 审查所有管理员账户
- 强制启用MFA
- 加强密码策略
- 审查权限分配

---

### 场景3: DDoS攻击 (P1)

**症状**:
- 服务器响应缓慢
- 大量并发请求
- 带宽耗尽

**立即行动**:
```bash
# 1. 启用CloudFlare DDoS防护 (如已接入)
# Web界面操作: Security → DDoS → Enable

# 2. Nginx限流
limit_req_zone $binary_remote_addr zone=ddos_protect:10m rate=1r/s;

server {
    location / {
        limit_req zone=ddos_protect burst=5 nodelay;
        limit_req_status 429;
    }
}

# 3. iptables连接限制
iptables -A INPUT -p tcp --dport 80 -m connlimit --connlimit-above 20 -j DROP

# 4. 分析攻击来源
tail -f /var/log/nginx/access.log | awk '{print $1}' | sort | uniq -c | sort -rn | head -20

# 5. 封禁攻击IP段
for ip in $(攻击IP列表); do
    iptables -A INPUT -s $ip -j DROP
done
```

**后续处理**:
- 联系ISP/云服务商
- 考虑接入专业DDoS防护
- 优化系统性能

---

### 场景4: 敏感数据泄露 (P0)

**症状**:
- 外部通报数据泄露
- 暗网发现公司数据
- 用户投诉隐私泄露

**立即行动** (1小时内):
```bash
# 1. 确认泄露范围
# 分析泄露的数据样本,确定来源

# 2. 停止数据外流
# 封禁可疑API
# 撤销可疑Token
# 审查数据导出日志

SELECT * FROM tb_audit_log
WHERE operation = 'EXPORT'
ORDER BY create_time DESC LIMIT 100;

# 3. 通知受影响用户 (GDPR要求72小时内)
# 批量发送通知

# 4. 报警 (如涉及刑事犯罪)
# 联系当地网安部门

# 5. 数据库加密 (亡羊补牢)
# 启用数据库加密
# 启用字段级加密
```

**法律合规**:
- **GDPR**: 72小时内通知监管机构
- **网络安全法**: 立即报告公安机关
- **个人信息保护法**: 通知受害者

---

### 场景5: 勒索软件攻击 (P0)

**症状**:
- 文件无法打开
- 桌面出现勒索信
- 文件被加密 (.locked扩展名)

**立即行动**:
```bash
# 1. 隔离受感染服务器
iptables -A INPUT -j DROP
iptables -A OUTPUT -j DROP

# 2. 不要支付赎金!

# 3. 从备份恢复
# 确保备份未被感染
# 从离线备份恢复

# 4. 查杀病毒
clamscan -r / --remove

# 5. 取证分析
# 保留原始磁盘镜像
# 联系专业安全公司
```

**预防措施**:
- 定期离线备份
- 最小权限原则
- 及时打补丁
- 用户安全培训

---

## 6. 通讯与上报

### 6.1 内部沟通

**沟通渠道**:
- **紧急**: 电话/短信
- **日常**: 企业微信群
- **正式**: 邮件

**沟通要点**:
- 使用统一模板
- 简洁明了
- 避免技术术语 (对非技术人员)
- 定时更新进展

**模板 - 事件通报**:
```
【安全事件通报】

级别: P1 - 高危
事件: 数据库异常访问
发现时间: 2025-11-24 14:35
当前状态: 已遏制,正在修复

影响:
- 部分用户无法登录
- 预计2小时内恢复

行动:
- 已封禁攻击源
- 正在修复漏洞
- 数据安全未受影响

下次更新: 16:00

如有疑问请联系:
安全团队: security@ct-tibet.com
```

---

### 6.2 外部上报

**监管机构上报** (P0/P1事件):

**1. 公安机关** (网安部门):
- 重大数据泄露
- 系统被破坏
- 网络攻击

**上报时限**: 立即 (24小时内)
**联系方式**: 110 / 当地网安支队

**2. 行业监管部门**:
- 工信部 (电信行业)
- 银保监会 (如涉及金融)

**上报时限**: 72小时内
**上报内容**:
- 事件经过
- 影响范围
- 处置措施
- 后续计划

---

### 6.3 用户沟通

**通知时机**:
- 数据泄露: 立即通知
- 服务中断: 提前通知 (如可预见)
- 安全风险: 及时提醒

**通知渠道**:
- 系统公告
- 短信/邮件
- APP推送

**通知模板**:
```
【重要安全通知】

尊敬的用户:

我们于2025年11月24日发现系统存在安全风险,已紧急处理。

您的账户可能受到影响,建议您:
1. 立即修改密码
2. 检查账户活动记录
3. 启用多因素认证

我们已采取以下措施:
1. 修复安全漏洞
2. 加强系统监控
3. 强化安全防护

如有疑问,请联系客服: 400-XXX-XXXX

再次为此带来的不便表示歉意。

西藏电信WMS团队
2025-11-24
```

---

## 7. 事后总结

### 7.1 事件报告模板

见第4.7节

### 7.2 改进措施跟踪

**改进措施表**:

| 编号 | 措施 | 负责人 | 截止日期 | 状态 | 备注 |
|------|------|--------|----------|------|------|
| 1 | 部署WAF | 运维组 | 2025-12-01 | 进行中 | |
| 2 | 代码审计 | 开发组 | 2025-12-07 | 未开始 | |
| 3 | 安全培训 | 安全组 | 2025-12-15 | 计划中 | |

**跟踪机制**:
- 每周例会检查进度
- 每月总结完成情况
- 纳入绩效考核

---

### 7.3 预防性措施

**技术措施**:
- 部署WAF
- 实施SAST/DAST
- 加强监控告警
- 定期漏洞扫描
- 数据加密备份

**管理措施**:
- 安全培训
- 代码审查
- 权限最小化
- 定期审计
- 应急演练

**流程优化**:
- 缩短响应时间
- 简化审批流程
- 自动化处理
- 知识库建设

---

## 8. 联系方式

### 8.1 应急联系人

**安全团队**:
- 安全负责人: 张三 - 138XXXX1111 - zhang.san@ct-tibet.com
- 安全工程师: 李四 - 138XXXX2222 - li.si@ct-tibet.com

**技术团队**:
- 技术负责人: 王五 - 138XXXX3333 - wang.wu@ct-tibet.com
- 后端负责人: 赵六 - 138XXXX4444 - zhao.liu@ct-tibet.com
- 运维负责人: 钱七 - 138XXXX5555 - qian.qi@ct-tibet.com

**管理团队**:
- CTO: 孙八 - 138XXXX6666 - sun.ba@ct-tibet.com
- 法务: 周九 - 138XXXX7777 - zhou.jiu@ct-tibet.com

**外部资源**:
- 云服务商: 400-XXX-XXXX
- 安全厂商: 400-YYY-YYYY
- 律师事务所: 010-ZZZZ-ZZZZ

### 8.2 报警电话

- **公安报警**: 110
- **网络举报**: 12377
- **工信部**: 12300
- **消费者投诉**: 12315

---

## 附录

### A. 事件响应工具箱

**必备工具**:
```bash
# 日志分析
tail, grep, awk, sed

# 网络分析
tcpdump, wireshark, netstat, ss

# 进程分析
ps, top, lsof, strace

# 文件分析
find, file, stat, md5sum

# 取证工具
dd, forensics-toolkit

# 安全扫描
nmap, nikto, sqlmap, burpsuite
```

**脚本示例**:
```bash
# incident-toolkit.sh
#!/bin/bash

# 快速收集事件证据
function collect_evidence() {
    EVIDENCE_DIR="/tmp/incident_$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$EVIDENCE_DIR"

    echo "收集证据到: $EVIDENCE_DIR"

    # 系统信息
    uname -a > "$EVIDENCE_DIR/system_info.txt"
    date >> "$EVIDENCE_DIR/system_info.txt"

    # 进程列表
    ps aux > "$EVIDENCE_DIR/processes.txt"

    # 网络连接
    netstat -antp > "$EVIDENCE_DIR/network.txt"

    # 登录记录
    last -100 > "$EVIDENCE_DIR/logins.txt"

    # 应用日志
    cp -r /app/logs "$EVIDENCE_DIR/"

    # 打包
    tar -czf "${EVIDENCE_DIR}.tar.gz" "$EVIDENCE_DIR"

    echo "证据已保存: ${EVIDENCE_DIR}.tar.gz"
}

# 调用
collect_evidence
```

---

### B. 应急演练计划

**演练频率**: 每半年一次

**演练场景**:
1. 数据泄露事件
2. 勒索软件攻击
3. DDoS攻击
4. 系统入侵

**演练流程**:
1. 准备 (2周): 制定演练方案
2. 通知 (1周): 通知参与人员
3. 执行 (2小时): 模拟事件响应
4. 总结 (1周): 编写演练报告

**评估指标**:
- 响应时间
- 处置正确性
- 团队协作
- 沟通效率

---

### C. 法律法规参考

- **中华人民共和国网络安全法**
- **中华人民共和国数据安全法**
- **中华人民共和国个人信息保护法**
- **等级保护2.0**
- **GDPR (General Data Protection Regulation)**

---

**文档结束**

**版本历史**:
- v1.0.0 (2025-11-24): 初始版本

**审批签字**:
- 编制: 安全团队
- 审核: CTO
- 批准: CEO

**下次修订**: 2026-06-01
