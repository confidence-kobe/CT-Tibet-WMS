# CT-Tibet-WMS 测试文档

## 测试概述

本项目包含全面的单元测试和集成测试，确保代码质量和功能正确性。

## 测试结构

```
src/test/java/com/ct/wms/
├── util/
│   └── TestDataBuilder.java           # 测试数据构建工具类
├── service/impl/
│   ├── StatisticsServiceImplTest.java # 统计服务单元测试
│   ├── MessageServiceImplTest.java    # 消息服务单元测试
│   └── UserServiceImplTest.java       # 用户服务单元测试
└── integration/
    └── ApplyOutboundFlowTest.java     # 申请出库流程集成测试
```

## 测试技术栈

- **JUnit 5**: 测试框架
- **Mockito**: Mock框架
- **AssertJ**: 流畅的断言API
- **Spring Boot Test**: Spring Boot测试支持
- **H2 Database**: 内存数据库（测试环境）

## 运行测试

### 运行所有测试

```bash
mvn test
```

### 运行特定测试类

```bash
# 运行统计服务测试
mvn test -Dtest=StatisticsServiceImplTest

# 运行消息服务测试
mvn test -Dtest=MessageServiceImplTest

# 运行用户服务测试
mvn test -Dtest=UserServiceImplTest

# 运行集成测试
mvn test -Dtest=ApplyOutboundFlowTest
```

### 运行特定测试方法

```bash
mvn test -Dtest=StatisticsServiceImplTest#testGetDashboardStats
```

### 查看测试覆盖率

```bash
mvn test jacoco:report
```

覆盖率报告位置: `target/site/jacoco/index.html`

## 测试配置

测试配置文件: `src/test/resources/application-test.yml`

- 使用H2内存数据库
- 自动建表
- 详细日志输出

## 主要测试用例

### 1. StatisticsServiceImplTest - 统计服务测试

**测试范围:**
- ✅ 仪表盘统计数据
- ✅ 入库统计（日期范围、仓库筛选、默认日期）
- ✅ 出库统计（类型筛选、趋势数据）
- ✅ 库存统计（周转率、Top10、预警数量）
- ✅ 物资分类占比

**测试用例:**
- `testGetDashboardStats()` - 测试获取仪表盘统计数据
- `testGetInboundStatistics_WithDateRange()` - 测试指定日期范围的入库统计
- `testGetInboundStatistics_WithWarehouse()` - 测试指定仓库的入库统计
- `testGetInboundStatistics_DefaultDateRange()` - 测试默认日期范围
- `testGetOutboundStatistics_WithType()` - 测试按出库类型筛选
- `testGetInventoryStatistics_TurnoverRate()` - 测试库存周转率计算
- `testGetInventoryStatistics_Top10()` - 测试Top 10库存占用排名
- `testGetInventoryStatistics_WarningCount()` - 测试预警数量统计

**关键验证点:**
- 统计数据准确性
- 日期序列完整性
- 趋势图数据正确性
- 排序和分组逻辑

### 2. MessageServiceImplTest - 消息服务测试

**测试范围:**
- ✅ 消息列表查询（带统计信息）
- ✅ 按类型和已读状态筛选
- ✅ 标记消息已读（单条、批量、全部）
- ✅ 发送消息
- ✅ 删除消息
- ✅ 权限控制

**测试用例:**
- `testListMyMessagesWithStats()` - 测试查询消息列表带统计信息
- `testListMyMessagesWithStats_FilterByType()` - 测试按消息类型筛选
- `testMarkAsRead_Success()` - 测试标记单条消息已读
- `testMarkAsRead_PermissionDenied()` - 测试无权操作消息
- `testBatchMarkAsRead()` - 测试批量标记消息已读
- `testMarkAllAsRead()` - 测试标记所有消息已读
- `testDeleteMessage_OnlyOwner()` - 测试只能删除自己的消息

**关键验证点:**
- 统计信息准确（total/unread/read）
- 权限控制（只能操作自己的消息）
- 状态更新正确性
- 筛选条件生效

### 3. UserServiceImplTest - 用户服务测试

**测试范围:**
- ✅ 用户CRUD操作
- ✅ 用户列表查询（部门、角色、关键字筛选）
- ✅ 用户状态管理
- ✅ 密码管理（重置、修改）
- ✅ 个人信息管理
- ✅ 业务规则校验

**测试用例:**
- `testListUsers_FilterByDept()` - 测试按部门筛选用户
- `testListUsers_SearchByKeyword()` - 测试关键字搜索
- `testCreateUser_Success()` - 测试创建用户成功
- `testCreateUser_UsernameExists()` - 测试用户名已存在
- `testUpdateCurrentUserProfile_Success()` - 测试更新个人信息
- `testChangeCurrentUserPassword_Success()` - 测试修改密码成功
- `testChangeCurrentUserPassword_WrongOldPassword()` - 测试旧密码错误
- `testChangeCurrentUserPassword_SamePassword()` - 测试新旧密码相同

**关键验证点:**
- 用户名唯一性
- 部门和角色关联
- 密码加密
- 旧密码验证
- 业务规则校验

### 4. ApplyOutboundFlowTest - 申请出库流程集成测试

**测试范围:**
- ✅ 完整申请出库流程
- ✅ 审批通过和拒绝场景
- ✅ 直接出库流程
- ✅ 库存不足场景
- ✅ 库存预警流程

**测试用例:**
- `testCompleteApplyOutboundFlow_Success()` - 测试完整申请出库流程
- `testApplyOutboundFlow_Rejected()` - 测试审批拒绝
- `testDirectOutboundFlow()` - 测试直接出库流程
- `testApplyOutboundFlow_InsufficientStock()` - 测试库存不足场景
- `testInventoryWarningFlow()` - 测试库存预警流程

**关键验证点:**
- 申请状态流转
- 库存扣减时机
- 出库单自动创建
- 权限控制
- 事务一致性

## 测试数据构建

使用`TestDataBuilder`工具类构建测试数据:

```java
// 创建测试用户
User user = TestDataBuilder.createUser(1L, "testuser", 1L, 2L);

// 创建测试物资
Material material = TestDataBuilder.createMaterial(1L, "测试物资", "分类",
    BigDecimal.valueOf(100));

// 模拟登录用户
TestDataBuilder.mockSecurityContext(userId, username, roleCode);

// 清除SecurityContext
TestDataBuilder.clearSecurityContext();
```

## 测试覆盖率目标

- **Service层**: > 80%
- **Controller层**: > 70%
- **关键业务逻辑**: 100%

## 测试最佳实践

### 1. 测试命名

使用 `test{MethodName}_{Scenario}` 格式:

```java
@Test
@DisplayName("测试入库统计 - 指定日期范围")
void testGetInboundStatistics_WithDateRange() {
    // ...
}
```

### 2. AAA模式

```java
@Test
void testExample() {
    // Arrange - 准备测试数据
    User user = TestDataBuilder.createUser(...);

    // Act - 执行测试
    UserDTO result = userService.getUserById(userId);

    // Assert - 验证结果
    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("testuser");
}
```

### 3. 使用Mock

```java
@Mock
private UserMapper userMapper;

@InjectMocks
private UserServiceImpl userService;

@Test
void testExample() {
    when(userMapper.selectById(1L)).thenReturn(user);

    // 执行测试

    verify(userMapper, times(1)).selectById(1L);
}
```

### 4. 异常测试

```java
@Test
void testCreateUser_UsernameExists() {
    // Given
    when(userMapper.selectCount(any())).thenReturn(1L);

    // When & Then
    assertThatThrownBy(() -> userService.createUser(dto))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("用户名已存在");
}
```

### 5. 参数化测试

```java
@ParameterizedTest
@CsvSource({
    "1, 测试用户1",
    "2, 测试用户2",
    "3, 测试用户3"
})
void testWithParameters(Long id, String name) {
    // 测试逻辑
}
```

## 持续集成

在CI/CD流程中自动运行测试:

```yaml
# GitHub Actions示例
- name: Run tests
  run: mvn test

- name: Generate coverage report
  run: mvn jacoco:report

- name: Upload coverage to Codecov
  uses: codecov/codecov-action@v3
```

## 常见问题

### Q: 测试失败 "未登录"

A: 确保在测试方法中调用了`TestDataBuilder.mockSecurityContext()`

### Q: H2数据库表不存在

A: 检查`application-test.yml`配置，确保MyBatis-Plus自动建表配置正确

### Q: 测试之间相互影响

A: 确保每个测试类或方法使用`@Transactional`注解，测试后自动回滚

### Q: Mock未生效

A: 检查是否使用了`@ExtendWith(MockitoExtension.class)`注解

## 扩展测试

### 添加新的测试类

1. 在`src/test/java/com/ct/wms/service/impl/`创建测试类
2. 使用`@ExtendWith(MockitoExtension.class)`
3. Mock依赖的Mapper和Service
4. 使用`@InjectMocks`注入被测试的Service
5. 编写测试用例

### 添加集成测试

1. 在`src/test/java/com/ct/wms/integration/`创建测试类
2. 使用`@SpringBootTest`和`@ActiveProfiles("test")`
3. 使用`@Transactional`确保测试隔离
4. 使用`@Autowired`注入真实的Bean
5. 编写端到端测试用例

## 贡献指南

提交代码前请确保:

1. ✅ 所有测试通过
2. ✅ 新功能有对应的测试用例
3. ✅ 测试覆盖率不降低
4. ✅ 遵循测试命名规范
5. ✅ 添加`@DisplayName`注解说明测试目的

## 参考资源

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
