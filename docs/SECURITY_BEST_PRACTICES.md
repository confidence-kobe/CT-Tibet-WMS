# CT-Tibet-WMS 安全最佳实践指南

**版本**: v1.0.0
**日期**: 2025-11-24
**目标受众**: 开发团队、运维团队、安全团队

---

## 目录

1. [认证与授权](#1-认证与授权)
2. [输入验证与输出编码](#2-输入验证与输出编码)
3. [数据保护](#3-数据保护)
4. [API安全](#4-api安全)
5. [会话管理](#5-会话管理)
6. [错误处理与日志](#6-错误处理与日志)
7. [配置管理](#7-配置管理)
8. [依赖管理](#8-依赖管理)
9. [代码审查](#9-代码审查)
10. [生产环境部署](#10-生产环境部署)

---

## 1. 认证与授权

### 1.1 密码安全

**DO ✅**:
```java
// 使用BCrypt加密密码
@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;  // BCrypt

    public void createUser(UserDTO dto) {
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        user.setPassword(encodedPassword);
    }
}
```

**DON'T ❌**:
```java
// 不要使用MD5/SHA1/SHA256直接加密
String password = DigestUtils.md5Hex(plainPassword);  // ❌ 不安全

// 不要存储明文密码
user.setPassword(dto.getPassword());  // ❌ 绝对禁止
```

**密码策略**:
```java
@Component
public class PasswordValidator {
    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    public boolean validate(String password) {
        if (password.length() < MIN_LENGTH) {
            throw new BusinessException("密码长度至少8位");
        }

        int strength = 0;
        if (UPPERCASE.matcher(password).find()) strength++;
        if (LOWERCASE.matcher(password).find()) strength++;
        if (DIGIT.matcher(password).find()) strength++;
        if (SPECIAL.matcher(password).find()) strength++;

        if (strength < 3) {
            throw new BusinessException("密码必须包含大小写字母、数字和特殊字符中的至少3种");
        }

        return true;
    }
}
```

---

### 1.2 JWT Token安全

**DO ✅**:
```java
// 生成强密钥 (至少256位)
@Value("${jwt.secret}")
private String secret;  // 从环境变量读取

// 验证密钥强度
@PostConstruct
public void init() {
    if (secret.length() < 64) {
        throw new IllegalStateException("JWT secret too short");
    }
}

// 设置合理的过期时间
public String generateToken(Long userId, String username) {
    return Jwts.builder()
        .setClaims(claims)
        .setExpiration(new Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000))  // 2小时
        .signWith(getSecretKey(), SignatureAlgorithm.HS256)
        .compact();
}
```

**DON'T ❌**:
```java
// 不要使用弱密钥
private String secret = "mysecret";  // ❌ 太短

// 不要硬编码密钥
private String secret = "hardcoded-secret-key-123";  // ❌ 硬编码

// 不要设置过长的有效期
.setExpiration(new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000))  // ❌ 1年太长
```

**Token黑名单实现**:
```java
@Service
public class TokenBlacklistService {
    private final RedisUtils redisUtils;

    public void blacklist(String token) {
        Claims claims = jwtUtils.getClaimsFromToken(token);
        Date expiration = claims.getExpiration();
        long ttl = (expiration.getTime() - System.currentTimeMillis()) / 1000;

        if (ttl > 0) {
            redisUtils.set("token:blacklist:" + token, "1", ttl);
        }
    }

    public boolean isBlacklisted(String token) {
        return redisUtils.hasKey("token:blacklist:" + token);
    }
}
```

---

### 1.3 权限控制

**DO ✅**:
```java
// 使用@PreAuthorize进行方法级权限控制
@PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN')")
public void deleteUser(Long id) {
    // 额外检查数据级权限
    User currentUser = SecurityUtils.getCurrentUser();
    User targetUser = userMapper.selectById(id);

    if (!currentUser.getRoleCode().equals("ADMIN")) {
        // 部门管理员只能删除本部门用户
        if (!currentUser.getDeptId().equals(targetUser.getDeptId())) {
            throw new BusinessException(403, "无权删除其他部门用户");
        }
    }

    userMapper.deleteById(id);
}
```

**DON'T ❌**:
```java
// 不要仅在Controller检查权限
@GetMapping("/{id}")
public Result<User> getUser(@PathVariable Long id) {
    // ❌ Service层可能被其他地方调用,绕过权限检查
    return Result.success(userService.getUser(id));
}
```

**数据级权限过滤**:
```java
@Component
@Aspect
public class DataPermissionAspect {

    @Around("@annotation(DataPermission)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        User currentUser = SecurityUtils.getCurrentUser();

        // 非管理员只能查询本部门数据
        if (!"ADMIN".equals(currentUser.getRoleCode())) {
            // 注入deptId条件
            injectDeptIdCondition(point, currentUser.getDeptId());
        }

        return point.proceed();
    }
}
```

---

## 2. 输入验证与输出编码

### 2.1 输入验证

**DO ✅**:
```java
@Data
public class UserDTO {
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名只能包含字母、数字、下划线,长度4-20位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Length(min = 8, max = 32, message = "密码长度必须在8-32位之间")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Length(max = 50, message = "真实姓名不能超过50字符")
    private String realName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "部门ID不能为空")
    @Min(value = 1, message = "部门ID必须大于0")
    private Long deptId;
}

// Controller中使用@Validated触发验证
@PostMapping
public Result<Long> createUser(@Validated @RequestBody UserDTO dto) {
    return Result.success(userService.createUser(dto));
}
```

**全局输入过滤器** (防XSS):
```java
@Component
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        XssHttpServletRequestWrapper wrappedRequest =
            new XssHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(wrappedRequest, response);
    }
}

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    @Override
    public String getParameter(String name) {
        return cleanXss(super.getParameter(name));
    }

    private String cleanXss(String value) {
        if (value == null) return null;

        // 移除常见XSS模式
        value = value.replaceAll("<script", "&lt;script");
        value = value.replaceAll("javascript:", "");
        value = value.replaceAll("onerror=", "");
        value = value.replaceAll("onload=", "");

        return value;
    }
}
```

---

### 2.2 SQL注入防护

**DO ✅**:
```java
// 使用MyBatis-Plus的LambdaQueryWrapper (自动参数化)
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getUsername, username)
       .like(User::getRealName, keyword)
       .orderByDesc(User::getCreateTime);

List<User> users = userMapper.selectList(wrapper);
```

**DON'T ❌**:
```java
// 不要拼接SQL字符串
String sql = "SELECT * FROM tb_user WHERE username = '" + username + "'";  // ❌ SQL注入风险

// 不要在MyBatis XML中使用 ${}
<select id="findUser">
    SELECT * FROM tb_user WHERE username = ${username}  <!-- ❌ SQL注入风险 -->
</select>
```

**动态排序字段白名单**:
```java
@Service
public class UserService {
    private static final Set<String> ALLOWED_ORDER_FIELDS =
        Set.of("id", "username", "create_time", "update_time");

    public Page<User> listUsers(String orderBy) {
        // 验证排序字段
        if (orderBy != null && !ALLOWED_ORDER_FIELDS.contains(orderBy)) {
            throw new BusinessException(400, "Invalid order field");
        }

        // 安全使用
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if ("id".equals(orderBy)) {
            wrapper.orderByDesc(User::getId);
        } else if ("create_time".equals(orderBy)) {
            wrapper.orderByDesc(User::getCreateTime);
        }

        return userMapper.selectPage(page, wrapper);
    }
}
```

---

### 2.3 输出编码

**DO ✅**:
```java
// 前端显示时自动转义
// Vue 3: {{ userInput }}  自动转义
// React: {userInput}      自动转义

// 手动转义HTML
import org.apache.commons.text.StringEscapeUtils;

public String escapeHtml(String input) {
    return StringEscapeUtils.escapeHtml4(input);
}
```

**DON'T ❌**:
```html
<!-- 不要使用 v-html / dangerouslySetInnerHTML -->
<div v-html="userInput"></div>  <!-- ❌ XSS风险 -->
```

---

## 3. 数据保护

### 3.1 敏感数据加密

**字段级加密**:
```java
@Component
public class FieldEncryptor {
    private final AES aes;

    public FieldEncryptor(@Value("${encryption.key}") String key) {
        this.aes = SecureUtil.aes(key.getBytes());
    }

    public String encrypt(String plainText) {
        if (plainText == null) return null;
        return aes.encryptHex(plainText);
    }

    public String decrypt(String cipherText) {
        if (cipherText == null) return null;
        return aes.decryptStr(cipherText);
    }
}

@Data
public class User {
    private Long id;
    private String username;

    @FieldEncrypt  // 自定义注解
    private String phone;  // 手机号加密存储

    @FieldEncrypt
    private String idCard;  // 身份证加密存储
}
```

**MyBatis拦截器自动加解密**:
```java
@Intercepts({
    @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class}),
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class EncryptionInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();

        // 查询结果自动解密
        if (invocation.getTarget() instanceof ResultSetHandler) {
            decryptFields(result);
        }

        // 插入/更新自动加密
        if (invocation.getTarget() instanceof Executor) {
            encryptFields(invocation.getArgs()[1]);
        }

        return result;
    }
}
```

---

### 3.2 数据脱敏

**日志脱敏**:
```java
@Slf4j
public class MaskingLogger {

    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\d{3})\\d{4}(\\d{4})");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(\\d{6})\\d{8}(\\d{4})");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(password|pwd)[\\s]*[:=][\\s]*([^,\\s}]+)");

    public static String mask(String message) {
        if (message == null) return null;

        // 手机号脱敏: 135****6789
        message = PHONE_PATTERN.matcher(message).replaceAll("$1****$2");

        // 身份证脱敏: 110101********1234
        message = ID_CARD_PATTERN.matcher(message).replaceAll("$1********$2");

        // 密码脱敏
        message = PASSWORD_PATTERN.matcher(message).replaceAll("$1:***");

        return message;
    }

    public static void info(String message, Object... args) {
        log.info(mask(message), args);
    }
}
```

**API响应脱敏**:
```java
@JsonSerialize(using = PhoneMaskSerializer.class)
private String phone;

public class PhoneMaskSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) {
        if (value != null && value.length() == 11) {
            gen.writeString(value.substring(0, 3) + "****" + value.substring(7));
        } else {
            gen.writeString(value);
        }
    }
}
```

---

### 3.3 数据库安全

**连接安全**:
```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/ct_tibet_wms?useSSL=true&requireSSL=true&verifyServerCertificate=true
    username: ${DB_USER}  # 专用账户
    password: ${DB_PASSWORD}  # 强密码
    hikari:
      connection-init-sql: "SELECT 1"  # 连接验证
      leak-detection-threshold: 10000  # 连接泄露检测
```

**SQL审计**:
```java
@Aspect
@Component
public class SqlAuditAspect {

    @Around("execution(* com.ct.wms.mapper.*.*(..))")
    public Object auditSql(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = point.proceed();
        long duration = System.currentTimeMillis() - start;

        // 记录慢查询
        if (duration > 1000) {
            log.warn("Slow SQL detected: {} - {}ms",
                point.getSignature().toShortString(), duration);
        }

        // 记录敏感操作
        if (point.getSignature().getName().contains("delete")) {
            auditService.logSensitiveOperation("DELETE", point.getArgs());
        }

        return result;
    }
}
```

---

## 4. API安全

### 4.1 速率限制

**Redis实现限流**:
```java
@Component
@Aspect
public class RateLimitAspect {
    private final RedisUtils redisUtils;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String key = "rate_limit:" + getClientId() + ":" + point.getSignature().toShortString();

        Long count = redisUtils.increment(key, 1L);
        if (count == 1) {
            redisUtils.expire(key, rateLimit.period());
        }

        if (count > rateLimit.limit()) {
            throw new BusinessException(429, "Too many requests");
        }

        return point.proceed();
    }
}

// 使用示例
@RateLimit(limit = 10, period = 60)  // 每分钟10次
@PostMapping("/login")
public Result<LoginVO> login(@RequestBody LoginRequest request) {
    return Result.success(authService.login(request));
}
```

**Nginx限流** (推荐):
```nginx
# nginx.conf
http {
    # 定义限流区域
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;
    limit_req_zone $binary_remote_addr zone=login_limit:10m rate=5r/m;

    server {
        # API接口限流
        location /api/ {
            limit_req zone=api_limit burst=20 nodelay;
            limit_req_status 429;
        }

        # 登录接口严格限流
        location /api/auth/login {
            limit_req zone=login_limit burst=2 nodelay;
        }
    }
}
```

---

### 4.2 CORS配置

**DO ✅**:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    // 明确指定允许的域名
    config.setAllowedOrigins(Arrays.asList(
        "https://wms.ct-tibet.com",
        "https://admin.ct-tibet.com"
    ));

    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

**DON'T ❌**:
```java
// 不要允许所有域名 + 携带凭证
config.setAllowedOriginPatterns(Collections.singletonList("*"));
config.setAllowCredentials(true);  // ❌ 危险组合
```

---

### 4.3 API版本控制

```java
@RestController
@RequestMapping("/api/v1/users")  // ✅ 包含版本号
public class UserControllerV1 {
    // ...
}

@RestController
@RequestMapping("/api/v2/users")  // 新版本
public class UserControllerV2 {
    // ...
}

// 或使用请求头版本控制
@GetMapping(headers = "API-Version=1")
public Result<User> getUserV1(@PathVariable Long id) {
    // ...
}
```

---

## 5. 会话管理

### 5.1 Token刷新策略

```java
@Service
public class TokenRefreshService {

    public LoginVO refreshToken(String oldToken) {
        // 1. 验证旧Token
        if (jwtUtils.isTokenExpired(oldToken)) {
            throw new BusinessException(401, "Token已过期");
        }

        // 2. 检查黑名单
        if (tokenBlacklistService.isBlacklisted(oldToken)) {
            throw new BusinessException(401, "Token已被撤销");
        }

        // 3. 验证Token类型
        Claims claims = jwtUtils.getClaimsFromToken(oldToken);
        if (!"refresh".equals(claims.get("type"))) {
            throw new BusinessException(401, "Invalid token type");
        }

        // 4. 生成新Token
        Long userId = jwtUtils.getUserIdFromToken(oldToken);
        String username = jwtUtils.getUsernameFromToken(oldToken);
        String newToken = jwtUtils.generateToken(userId, username);

        // 5. 旧Token加入黑名单
        tokenBlacklistService.blacklist(oldToken);

        return buildLoginVO(newToken, userId, username);
    }
}
```

---

### 5.2 强制下线机制

```java
@Service
public class SessionManagementService {

    // 管理员强制用户下线
    public void forceLogout(Long userId) {
        // 1. 获取用户的所有Token
        Set<String> tokens = redisUtils.getSet("user:tokens:" + userId);

        // 2. 加入黑名单
        tokens.forEach(tokenBlacklistService::blacklist);

        // 3. 清空用户Token列表
        redisUtils.delete("user:tokens:" + userId);

        // 4. 记录审计日志
        auditService.logForceLogout(getCurrentUserId(), userId);
    }

    // 用户登录时记录Token
    public void recordToken(Long userId, String token) {
        String key = "user:tokens:" + userId;
        redisUtils.addToSet(key, token);

        // 设置过期时间 (与Token一致)
        redisUtils.expire(key, jwtExpiration);
    }
}
```

---

## 6. 错误处理与日志

### 6.1 安全的异常处理

**DO ✅**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active}")
    private String profile;

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        // 记录详细错误
        log.error("Unexpected exception: {}", e.getMessage(), e);

        // 生产环境返回通用错误
        if ("prod".equals(profile)) {
            return Result.error(500, "系统异常,请联系管理员");
        }

        // 开发环境返回详细错误
        return Result.error(500, e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        // 业务异常可返回详细信息
        return Result.error(e.getErrorCode(), e.getMessage());
    }
}
```

**DON'T ❌**:
```java
// 不要返回堆栈跟踪
return Result.error(500, e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));

// 不要暴露SQL错误
catch (SQLException e) {
    return Result.error(500, "SQL错误: " + e.getMessage());  // ❌ 泄露表结构
}
```

---

### 6.2 安全日志实践

**DO ✅**:
```java
// 记录审计日志
@Aspect
@Component
public class AuditLogAspect {

    @AfterReturning("@annotation(auditLog)")
    public void after(JoinPoint point, AuditLog auditLog) {
        User currentUser = SecurityUtils.getCurrentUser();

        AuditLogEntity log = AuditLogEntity.builder()
            .userId(currentUser.getId())
            .username(currentUser.getUsername())
            .operation(auditLog.value())
            .method(point.getSignature().toShortString())
            .params(JSON.toJSONString(point.getArgs()))
            .ip(RequestUtils.getClientIp())
            .userAgent(RequestUtils.getUserAgent())
            .timestamp(LocalDateTime.now())
            .build();

        auditLogService.save(log);
    }
}

// 使用示例
@AuditLog("删除用户")
@DeleteMapping("/{id}")
public Result<Void> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return Result.success();
}
```

**DON'T ❌**:
```java
// 不要记录敏感信息
log.info("用户登录: username={}, password={}", username, password);  // ❌

// 不要记录完整Token
log.info("Token验证成功: {}", token);  // ❌
```

**日志脱敏**:
```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <encoder class="com.ct.wms.logging.MaskingPatternLayoutEncoder">
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
            <maskPatterns>
                <pattern>(password|pwd)[\\s]*[:=][\\s]*([^,\\s}]+)</pattern>
                <pattern>(token)[\\s]*[:=][\\s]*([^,\\s}]+)</pattern>
                <pattern>(\\d{3})\\d{4}(\\d{4})</pattern>
            </maskPatterns>
        </encoder>
    </appender>
</configuration>
```

---

## 7. 配置管理

### 7.1 敏感配置保护

**DO ✅**:
```yaml
# application-prod.yml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}

jwt:
  secret: ${JWT_SECRET}

redis:
  password: ${REDIS_PASSWORD}
```

**使用Docker Secrets**:
```yaml
# docker-compose.yml
services:
  backend:
    environment:
      DB_PASSWORD_FILE: /run/secrets/db_password
      JWT_SECRET_FILE: /run/secrets/jwt_secret
    secrets:
      - db_password
      - jwt_secret

secrets:
  db_password:
    external: true
  jwt_secret:
    external: true
```

**DON'T ❌**:
```yaml
# 不要提交到Git
spring:
  datasource:
    password: MyP@ssw0rd  # ❌ 硬编码密码
```

---

### 7.2 环境隔离

```
project/
├── src/main/resources/
│   ├── application.yml           # 公共配置
│   ├── application-dev.yml       # 开发环境
│   ├── application-test.yml      # 测试环境
│   └── application-prod.yml      # 生产环境
├── .gitignore
│   └── *-local.yml              # 排除本地配置
└── config/
    ├── dev/
    ├── test/
    └── prod/
        └── secrets.yml           # 敏感配置 (不提交)
```

---

## 8. 依赖管理

### 8.1 依赖安全扫描

```xml
<!-- pom.xml -->
<build>
    <plugins>
        <!-- OWASP Dependency-Check -->
        <plugin>
            <groupId>org.owasp</groupId>
            <artifactId>dependency-check-maven</artifactId>
            <version>9.0.7</version>
            <configuration>
                <failBuildOnCVSS>7</failBuildOnCVSS>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**定期检查**:
```bash
# 检查依赖更新
mvn versions:display-dependency-updates

# 检查插件更新
mvn versions:display-plugin-updates

# 扫描漏洞
mvn dependency-check:check
```

---

### 8.2 依赖版本管理

**DO ✅**:
```xml
<properties>
    <spring-boot.version>2.7.18</spring-boot.version>  <!-- 明确版本 -->
    <mybatis-plus.version>3.5.5</mybatis-plus.version>
</properties>

<dependencyManagement>
    <dependencies>
        <!-- 统一管理版本 -->
    </dependencies>
</dependencyManagement>
```

**DON'T ❌**:
```xml
<!-- 不要使用LATEST/RELEASE -->
<version>LATEST</version>  <!-- ❌ 不可预测 -->
<version>1.+</version>     <!-- ❌ 不可预测 -->
```

---

## 9. 代码审查

### 9.1 安全审查清单

**审查要点**:
- [ ] 敏感操作是否有权限检查
- [ ] 输入是否经过验证
- [ ] SQL是否参数化
- [ ] 密码是否加密存储
- [ ] Token是否安全生成
- [ ] 日志是否包含敏感信息
- [ ] 错误信息是否暴露细节
- [ ] 配置文件是否包含密码
- [ ] 文件上传是否验证类型
- [ ] API是否有速率限制

---

### 9.2 自动化检查

**Pre-commit Hook**:
```bash
#!/bin/bash
# .git/hooks/pre-commit

echo "Running security checks..."

# 检查敏感信息
if git diff --cached | grep -E "(password|secret|key)[\\s]*[:=][\\s]*['\"][^'\"]+" ; then
    echo "❌ Found potential secrets in code"
    exit 1
fi

# 运行Spotbugs
mvn spotbugs:check

# 运行单元测试
mvn test

echo "✅ Security checks passed"
```

---

## 10. 生产环境部署

### 10.1 部署前检查

**安全检查清单**:
```bash
# 1. 依赖漏洞扫描
mvn dependency-check:check

# 2. 容器镜像扫描
trivy image ct-tibet-wms:latest

# 3. 敏感信息扫描
gitleaks detect

# 4. 静态代码分析
mvn spotbugs:check

# 5. 配置检查
grep -r "password.*root" backend/src/main/resources/
```

---

### 10.2 生产环境配置

**最小化暴露**:
```yaml
# application-prod.yml
server:
  port: 48888
  error:
    include-stacktrace: never  # 不返回堆栈
    include-exception: false

knife4j:
  enable: false  # 禁用Swagger

management:
  endpoints:
    web:
      exposure:
        include: health  # 仅暴露健康检查
```

**HTTPS强制**:
```nginx
server {
    listen 80;
    server_name wms.ct-tibet.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name wms.ct-tibet.com;

    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload" always;
}
```

---

## 总结

**安全开发生命周期**:
1. **设计阶段**: 威胁建模、安全架构设计
2. **开发阶段**: 安全编码、代码审查
3. **测试阶段**: 安全测试、渗透测试
4. **部署阶段**: 配置加固、环境隔离
5. **运维阶段**: 监控告警、应急响应

**持续改进**:
- 定期安全培训 (每季度)
- 定期安全扫描 (每周)
- 定期渗透测试 (每年)
- 定期应急演练 (每半年)

**安全文化**:
- Security is everyone's responsibility
- Shift-left security
- Defense in depth
- Least privilege
- Fail securely

---

**文档结束**

**参考资源**:
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [OWASP ASVS](https://owasp.org/www-project-application-security-verification-standard/)
- [CWE Top 25](https://cwe.mitre.org/top25/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
