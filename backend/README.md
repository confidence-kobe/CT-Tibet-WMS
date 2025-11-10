# CT Tibet WMS - 后端项目

西藏电信仓库管理系统后端服务

## 技术栈

- **Spring Boot** 2.7.18
- **MyBatis-Plus** 3.5.5
- **MySQL** 8.0+
- **Redis** 6.0+
- **RabbitMQ** 3.12+
- **Spring Security + JWT**
- **Knife4j (Swagger)**

## 项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/ct/wms/
│   │   │   ├── WmsApplication.java       # 启动类
│   │   │   ├── common/                   # 公共模块
│   │   │   │   ├── api/                  # 统一响应
│   │   │   │   ├── constant/             # 常量
│   │   │   │   ├── exception/            # 异常处理
│   │   │   │   └── annotation/           # 自定义注解
│   │   │   ├── config/                   # 配置类
│   │   │   ├── security/                 # 安全模块
│   │   │   ├── controller/               # 控制器
│   │   │   ├── service/                  # 业务层
│   │   │   ├── mapper/                   # 数据访问层
│   │   │   ├── entity/                   # 实体类
│   │   │   ├── dto/                      # 数据传输对象
│   │   │   ├── vo/                       # 视图对象
│   │   │   └── utils/                    # 工具类
│   │   └── resources/
│   │       ├── application.yml           # 主配置
│   │       ├── application-dev.yml       # 开发环境
│   │       ├── application-prod.yml      # 生产环境
│   │       └── mapper/                   # MyBatis映射
│   └── test/                             # 测试代码
└── pom.xml                               # Maven配置
```

## 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.12+ (可选)

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/ct-tibet/wms.git
cd CT-Tibet-WMS/backend
```

### 2. 初始化数据库

```bash
# 执行数据库脚本
cd ../sql
mysql -u root -p < create_database.sql
mysql -u root -p ct_tibet_wms < schema.sql
mysql -u root -p ct_tibet_wms < init_data.sql
```

### 3. 修改配置

编辑 `src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    username: root
    password: your_password  # 修改为你的MySQL密码
  redis:
    password: your_redis_password  # 如果Redis有密码
```

### 4. 启动项目

```bash
# 方式1: Maven命令
mvn clean install
mvn spring-boot:run

# 方式2: IDE运行
# 直接运行 WmsApplication.java 的 main 方法
```

### 5. 访问应用

- **应用地址**: http://localhost:8080
- **API文档**: http://localhost:8080/doc.html
- **健康检查**: http://localhost:8080/actuator/health

## 默认账号

| 角色 | 用户名 | 密码 |
|-----|--------|------|
| 系统管理员 | admin | 123456 |
| 仓库管理员 | wl_warehouse | 123456 |
| 普通员工 | wl_user1 | 123456 |

## API文档

启动项目后访问 Knife4j 文档:

```
http://localhost:8080/doc.html
```

## 开发指南

### 新增业务模块

1. 创建实体类 (entity)
2. 创建Mapper接口 (mapper)
3. 创建Service接口和实现 (service)
4. 创建DTO和VO (dto, vo)
5. 创建Controller (controller)
6. 编写单元测试 (test)

### 代码规范

- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- 所有公共方法必须有JavaDoc注释
- Controller方法使用@Operation注解
- 使用统一的Result返回格式

### 异常处理

```java
// 业务异常
throw new BusinessException(ErrorCode.USER_NOT_FOUND);

// 带自定义消息
throw new BusinessException(ErrorCode.INVALID_PARAM, "用户名不能为空");
```

### 操作日志

```java
@Log(title = "用户管理", businessType = BusinessType.INSERT)
@PostMapping
public Result<Void> createUser(@RequestBody UserDTO dto) {
    // 方法会自动记录操作日志
}
```

## 构建部署

### 打包

```bash
mvn clean package -DskipTests
```

生成文件: `target/ct-tibet-wms.jar`

### Docker部署

```bash
# 构建镜像
docker build -t ct-tibet-wms:1.0.0 .

# 运行容器
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=mysql \
  -e DB_PASSWORD=your_password \
  --name wms-backend \
  ct-tibet-wms:1.0.0
```

### Docker Compose部署

```bash
cd ../deploy
docker-compose up -d
```

## 测试

```bash
# 运行所有测试
mvn test

# 运行单个测试
mvn test -Dtest=UserServiceTest
```

## 性能优化

1. **数据库连接池**: 使用HikariCP,已优化配置
2. **Redis缓存**: 常用数据缓存,减少数据库查询
3. **异步处理**: 使用@Async注解处理耗时操作
4. **分页查询**: 使用MyBatis-Plus分页插件
5. **SQL优化**: 添加合适的索引,避免全表扫描

## 监控

使用Spring Boot Actuator:

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 应用信息
curl http://localhost:8080/actuator/info

# 指标信息
curl http://localhost:8080/actuator/metrics
```

## 常见问题

### 1. 数据库连接失败

检查:
- MySQL服务是否启动
- 数据库连接配置是否正确
- 防火墙是否开放3306端口

### 2. Redis连接失败

检查:
- Redis服务是否启动
- Redis密码配置是否正确
- 防火墙是否开放6379端口

### 3. 启动报错找不到Mapper

确保:
- @MapperScan注解配置正确
- Mapper接口位于指定包下
- MyBatis配置正确

## 相关文档

- [需求分析文档](../docs/需求分析.md)
- [PRD产品需求文档](../docs/PRD-产品需求文档.md)
- [数据库设计文档](../docs/数据库设计文档.md)
- [API接口文档](../docs/API接口文档.md)
- [项目架构文档](../docs/项目架构文档.md)

## 技术支持

如有问题,请联系:
- 技术团队: dev@chinatelecom.cn
- 项目负责人: tech-lead@chinatelecom.cn

---

**版本**: v1.0.0
**最后更新**: 2025-11-11
**维护团队**: CT Tibet Development Team
