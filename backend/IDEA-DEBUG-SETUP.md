# IDEA 调试配置 - 日志分析指南

## 概述

本文档说明如何在 IntelliJ IDEA 中启动调试，并让 Claude Code 分析日志中的异常。

## 日志文件说明

当你在 IDEA 中启动应用后，日志会自动输出到以下位置：

```
backend/logs/
├── all.log                  # 所有级别的日志
├── error.log                # 仅错误日志（ERROR级别）
├── debug-for-claude.log     # 🎯 Claude专用调试日志（包含完整堆栈信息）
├── sql.log                  # SQL语句执行日志
└── business.log             # 业务逻辑日志
```

## IDEA 调试启动步骤

### 方法一：直接运行主类

1. 打开 `backend/src/main/java/com/ct/wms/WmsApplication.java`
2. 右键点击类名或 `main` 方法
3. 选择 **"Debug 'WmsApplication'"**
4. 等待应用启动完成

### 方法二：使用 Maven 配置

1. 点击 IDEA 右上角的 **"Add Configuration..."**
2. 点击 **"+"** → 选择 **"Spring Boot"**
3. 配置如下：
   - **Name**: WMS Backend Debug
   - **Main class**: `com.ct.wms.WmsApplication`
   - **Working directory**: `$MODULE_WORKING_DIR$`
   - **JRE**: Java 17
   - **Environment variables** (可选):
     ```
     DB_PASSWORD=root
     REDIS_HOST=localhost
     RABBITMQ_HOST=localhost
     ```
4. 点击 **"Apply"** → **"OK"**
5. 点击右上角的调试按钮（绿色虫子图标）

### 方法三：使用 Maven 命令

在 IDEA Terminal 中执行：
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

## 日志实时监控

### 在 IDEA 中查看日志

启动应用后，IDEA 控制台会显示彩色日志输出。

### 使用外部工具监控日志文件

**PowerShell** (推荐):
```powershell
# 实时查看所有日志
Get-Content backend\logs\all.log -Wait -Tail 100

# 实时查看错误日志
Get-Content backend\logs\error.log -Wait -Tail 50

# 实时查看Claude调试日志
Get-Content backend\logs\debug-for-claude.log -Wait -Tail 100
```

**CMD**:
```cmd
powershell Get-Content backend\logs\debug-for-claude.log -Wait -Tail 100
```

## Claude 日志分析流程

### 当遇到异常时

1. **保持应用运行**（或重现问题后停止）

2. **告诉 Claude 分析日志**：
   ```
   帮我分析 backend/logs/debug-for-claude.log 中的异常
   ```

3. **Claude 会自动**：
   - 读取日志文件
   - 定位异常堆栈
   - 分析根本原因
   - 提供修复建议

### 常见分析场景

#### 场景 1：应用启动失败
```
应用启动失败了，帮我看看 debug-for-claude.log 最后 50 行
```

#### 场景 2：特定功能报错
```
入库操作报错了，帮我在 debug-for-claude.log 中搜索 "InboundController" 相关的错误
```

#### 场景 3：数据库相关问题
```
数据库连接失败，帮我分析 error.log 和 sql.log
```

#### 场景 4：完整堆栈分析
```
帮我分析 debug-for-claude.log 中的所有 ERROR 和 WARN 日志
```

## 日志级别说明

| 级别 | 用途 | 示例场景 |
|------|------|---------|
| **DEBUG** | 详细调试信息 | 方法调用、参数值、业务流程 |
| **INFO** | 关键信息 | 应用启动、请求处理、业务成功 |
| **WARN** | 警告信息 | 潜在问题、降级处理 |
| **ERROR** | 错误信息 | 异常、失败操作 |

## 日志配置文件

日志配置位于：
- **主配置**: `backend/src/main/resources/logback-spring.xml`
- **应用配置**: `backend/src/main/resources/application.yml`

### 调整日志级别（临时）

在 IDEA 启动前，修改 `application.yml` 中的 active profile：
```yaml
spring:
  profiles:
    active: dev  # dev=DEBUG级别, prod=INFO级别
```

### 查看特定包的详细日志

编辑 `logback-spring.xml`，添加：
```xml
<logger name="com.ct.wms.controller" level="DEBUG"/>
<logger name="com.ct.wms.service" level="DEBUG"/>
```

## 调试技巧

### 1. 断点调试 + 日志分析结合

- 在可能出错的地方打断点
- 运行到断点时，Claude 已经在后台记录日志
- 继续执行后，让 Claude 分析日志找出问题

### 2. 条件日志输出

在代码中添加调试日志：
```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InboundService {
    public void create(InboundDTO dto) {
        log.debug("创建入库单 - 参数: {}", dto);
        try {
            // 业务逻辑
            log.info("入库单创建成功 - ID: {}", result.getId());
        } catch (Exception e) {
            log.error("入库单创建失败 - 参数: {}, 异常: {}", dto, e.getMessage(), e);
            throw e;
        }
    }
}
```

### 3. 性能问题分析

查看 `all.log` 中的时间戳，找出慢操作：
```
2025-11-13 14:00:00.100 - 开始查询
2025-11-13 14:00:03.500 - 查询完成  ← 3.4秒，可能有性能问题
```

## 常见问题

### Q1: 日志文件太大怎么办？

**A**: 日志会自动滚动，配置如下：
- 单文件最大 100-200MB
- 保留 7-30 天历史
- 超过限制自动清理旧文件

### Q2: 没有生成日志文件？

**A**: 检查以下项：
1. 确认 `backend/logs/` 目录存在
2. 确认应用已启动（查看 IDEA 控制台）
3. 确认 `logback-spring.xml` 配置正确
4. 检查文件权限

### Q3: Claude 分析日志速度慢？

**A**: 优化策略：
- 只分析最近的日志（最后 100-500 行）
- 指定具体的错误类型或关键字
- 使用 `error.log` 而不是 `all.log`

### Q4: 如何只看最新的错误？

**A**:
```bash
# PowerShell
Get-Content backend\logs\error.log -Tail 50
```

或告诉 Claude：
```
只分析 error.log 最后 50 行
```

## 快速命令参考

```bash
# 查看最新50行所有日志
Get-Content backend\logs\all.log -Tail 50

# 查看最新错误
Get-Content backend\logs\error.log -Tail 30

# 实时监控（跟随新日志）
Get-Content backend\logs\debug-for-claude.log -Wait -Tail 100

# 搜索特定关键字
Select-String -Path backend\logs\all.log -Pattern "NullPointerException" -Context 5

# 统计错误数量
(Get-Content backend\logs\error.log | Select-String "ERROR").Count
```

## 示例：完整的调试工作流

1. **启动应用**
   ```
   在 IDEA 中 Debug 运行 WmsApplication
   ```

2. **触发问题**
   ```
   访问 http://localhost:48888/api/inbound/create
   ```

3. **查看日志**
   ```powershell
   Get-Content backend\logs\error.log -Tail 20
   ```

4. **Claude 分析**
   ```
   帮我分析 backend/logs/debug-for-claude.log 中关于 "InboundController" 的最新错误
   ```

5. **根据建议修复**
   ```
   Claude 会告诉你具体的错误原因和修复建议
   ```

6. **验证修复**
   ```
   重启应用，再次测试，确认 error.log 中没有新错误
   ```

---

## 联系 Claude

在对话中直接说：
- "分析最新的错误日志"
- "为什么应用启动失败？"
- "帮我看看数据库连接问题"
- "SQL日志中有什么异常？"

Claude 会自动读取相应的日志文件并提供详细分析！
