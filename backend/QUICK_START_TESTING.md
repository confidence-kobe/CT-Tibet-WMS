# 测试快速入门指南

## 5分钟快速开始

### Step 1: 验证环境
```bash
cd H:\java\CT-Tibet-WMS\backend

# 检查Maven
mvn -version

# 检查Java
java -version
```

### Step 2: 运行所有测试
```bash
# Windows
run-tests.bat
# 选择 [1] Run all tests

# Linux/Mac
./run-tests.sh
# 选择 [1] Run all tests

# 或直接使用Maven
mvn test
```

### Step 3: 查看结果
测试通过会显示:
```
[INFO] Tests run: 49, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Step 4: 查看覆盖率
```bash
# 生成覆盖率报告
mvn test jacoco:report

# 打开报告
# Windows: start target\site\jacoco\index.html
# Mac: open target/site/jacoco/index.html
# Linux: xdg-open target/site/jacoco/index.html
```

## 测试文件位置

```
backend/
├── src/test/
│   ├── java/com/ct/wms/
│   │   ├── util/TestDataBuilder.java              # 测试工具类
│   │   ├── service/impl/
│   │   │   ├── StatisticsServiceImplTest.java     # 统计服务测试
│   │   │   ├── MessageServiceImplTest.java        # 消息服务测试
│   │   │   └── UserServiceImplTest.java           # 用户服务测试
│   │   └── integration/
│   │       └── ApplyOutboundFlowTest.java         # 集成测试
│   └── resources/
│       └── application-test.yml                    # 测试配置
├── run-tests.bat                                   # Windows测试脚本
├── run-tests.sh                                    # Linux/Mac测试脚本
├── README_TEST.md                                  # 详细测试文档
├── TEST_SUMMARY.md                                 # 测试总结
└── TEST_FILES_CHECKLIST.md                        # 文件清单
```

## 常用测试命令

### 运行所有测试
```bash
mvn test
```

### 运行单个测试类
```bash
mvn test -Dtest=StatisticsServiceImplTest
mvn test -Dtest=MessageServiceImplTest
mvn test -Dtest=UserServiceImplTest
```

### 运行单个测试方法
```bash
mvn test -Dtest=StatisticsServiceImplTest#testGetDashboardStats
```

### 运行单元测试
```bash
mvn test -Dtest='*ServiceImplTest'
```

### 运行集成测试
```bash
mvn test -Dtest='*FlowTest'
```

### 跳过测试
```bash
mvn clean install -DskipTests
```

### 查看详细输出
```bash
mvn test -X
```

## 测试覆盖的功能

### 1. 统计服务 (StatisticsServiceImplTest)
- ✅ 仪表盘统计
- ✅ 入库统计(日期、仓库筛选)
- ✅ 出库统计(类型筛选)
- ✅ 库存统计(周转率、Top10)

### 2. 消息服务 (MessageServiceImplTest)
- ✅ 消息列表查询
- ✅ 消息筛选(类型、状态)
- ✅ 消息状态管理
- ✅ 权限控制

### 3. 用户服务 (UserServiceImplTest)
- ✅ 用户CRUD
- ✅ 个人信息管理
- ✅ 密码管理
- ✅ 权限验证

### 4. 业务流程 (ApplyOutboundFlowTest)
- ✅ 申请出库流程
- ✅ 审批流程
- ✅ 库存管理

## 故障排查

### 问题1: 测试失败 "未登录"
**解决方案:**
```java
@BeforeEach
void setUp() {
    TestDataBuilder.mockSecurityContext(userId, username, roleCode);
}

@AfterEach
void tearDown() {
    TestDataBuilder.clearSecurityContext();
}
```

### 问题2: H2数据库错误
**解决方案:**
检查 `application-test.yml` 配置:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL
```

### 问题3: 测试之间相互影响
**解决方案:**
添加 `@Transactional` 注解:
```java
@SpringBootTest
@Transactional
class MyTest {
    // 测试方法
}
```

## 更多信息

- 详细文档: `README_TEST.md`
- 测试总结: `TEST_SUMMARY.md`
- 文件清单: `TEST_FILES_CHECKLIST.md`

## 支持

如有问题,请查看文档或提交Issue。

Happy Testing! 🎉
