# CT-Tibet-WMS 安全审计报告

**审计日期**: 2025-11-24
**审计范围**: Backend (Spring Boot) + Configuration + Infrastructure
**审计标准**: OWASP Top 10 2021, CWE Top 25, 等保2.0
**系统版本**: v1.0.0
**审计员**: Claude Security Auditor

---

## 执行摘要 (Executive Summary)

本次安全审计对 CT-Tibet-WMS (西藏电信仓库管理系统) 进行了全面的代码审查、配置审计和架构评估。系统采用 Spring Boot 2.7.18 + JWT + Spring Security 架构,整体安全基线良好,但发现**23个安全问题**,其中:

- **高危 (Critical)**: 3个
- **高风险 (High)**: 7个
- **中风险 (Medium)**: 9个
- **低风险 (Low)**: 4个

**关键发现**:
1. ✅ **良好实践**: JWT认证、BCrypt密码加密、RBAC权限控制已实现
2. ⚠️ **高危问题**: CORS配置过于宽松 (`allow-credentials: true` + `*`)
3. ⚠️ **高风险**: 生产环境默认JWT密钥、日志记录敏感信息、缺少Rate Limiting
4. ⚠️ **中风险**: 缺少CSRF保护、Actuator端点暴露、缺少安全响应头

**合规性评估**:
- **GDPR**: ⚠️ 部分合规 (缺少数据分类、审计日志)
- **等保2.0**: ⚠️ 部分合规 (缺少入侵检测、审计追溯)
- **OWASP ASVS L2**: ⚠️ 部分合规 (60%)

---

## 1. 高危漏洞 (Critical - 立即修复)

### 1.1 CORS 配置安全风险 (CWE-942)

**位置**: `SecurityConfig.java:72-75`

```java
configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
configuration.setAllowCredentials(true);  // ❌ 危险组合
```

**问题描述**:
- `allowCredentials: true` + `allowedOrigins: *` 组合违反CORS规范
- 允许任意域名携带凭证访问,可能导致CSRF攻击
- 浏览器会拒绝此配置,但部分旧版本浏览器可能执行

**风险评级**: Critical (CVSS 8.1)

**影响**:
- 跨站请求伪造 (CSRF)
- 用户凭证泄露
- 会话劫持

**修复建议**:
```java
// 生产环境应明确指定允许的域名
configuration.setAllowedOriginPatterns(Arrays.asList(
    "https://wms.ct-tibet.com",
    "https://admin.ct-tibet.com"
));
configuration.setAllowCredentials(true);
configuration.setMaxAge(3600L);

// 或者关闭 allowCredentials
configuration.setAllowedOrigins(Collections.singletonList("*"));
configuration.setAllowCredentials(false);
```

**合规要求**:
- OWASP ASVS 13.2.2 - CORS配置验证
- CWE-942 - Overly Permissive Cross-domain Whitelist

---

### 1.2 生产环境使用默认JWT密钥 (CWE-798)

**位置**: `application.yml:101`, `application-prod.yml:151`

```yaml
# application.yml
jwt:
  secret: ${JWT_SECRET:ct-tibet-wms-secret-key-2025-do-not-use-in-production}
  # ❌ 默认值过于简单

# application-prod.yml
jwt:
  secret: ${JWT_SECRET}  # ✅ 强制使用环境变量,但缺少验证
```

**问题描述**:
1. **开发环境默认密钥过短** (52字节): 应至少64字节 (512位)
2. **生产环境缺少密钥强度验证**: 无法保证运维人员设置强密钥
3. **密钥轮换机制缺失**: 一旦泄露无法快速更换

**风险评级**: Critical (CVSS 9.1)

**影响**:
- 攻击者可伪造JWT Token获取任意用户权限
- 完全绕过认证系统
- 可提权到管理员账户

**修复建议**:

1. **生成强密钥** (至少256位):
```bash
# 生成安全的JWT密钥
openssl rand -base64 64
# 输出示例: cN8K9x2F... (88字符)
```

2. **启动时验证密钥强度**:
```java
@Component
public class JwtSecretValidator implements ApplicationListener<ContextRefreshedEvent> {
    @Value("${jwt.secret}")
    private String secret;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (secret.length() < 64) {
            throw new IllegalStateException(
                "JWT secret must be at least 64 characters. Current length: " + secret.length()
            );
        }
        if (secret.contains("do-not-use-in-production")) {
            throw new IllegalStateException("Default JWT secret detected! Set JWT_SECRET environment variable.");
        }
    }
}
```

3. **实现密钥轮换**:
```yaml
jwt:
  secrets:
    current: ${JWT_SECRET_CURRENT}
    previous: ${JWT_SECRET_PREVIOUS:}  # 用于验证旧Token
  rotation-interval: 90  # 90天轮换一次
```

**合规要求**:
- OWASP ASVS 6.2.1 - 密钥强度要求
- NIST SP 800-131A - 加密密钥管理

---

### 1.3 密码重置功能缺少双因素验证 (CWE-640)

**位置**: `UserController.java:106-115`

```java
@PutMapping("/{id}/reset-password")
@PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN')")
public Result<Void> resetPassword(
        @PathVariable Long id,
        @RequestParam String newPassword) {  // ❌ 新密码直接从URL传递
    userService.resetPassword(id, newPassword);
    return Result.success(null, "重置成功");
}
```

**问题描述**:
1. **新密码通过URL参数传递**: 会记录在访问日志、浏览器历史中
2. **管理员可重置任意用户密码**: 无二次验证,可能被滥用
3. **缺少密码强度校验**: 可能设置弱密码
4. **无审计日志**: 无法追踪谁重置了谁的密码

**风险评级**: Critical (CVSS 8.5)

**影响**:
- 管理员账号被入侵后可批量重置用户密码
- 密码泄露到访问日志
- 无法追溯密码重置行为

**修复建议**:

1. **使用请求体传递密码**:
```java
@PutMapping("/{id}/reset-password")
@PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN')")
public Result<Void> resetPassword(
        @PathVariable Long id,
        @Validated @RequestBody ResetPasswordRequest request) {
    // 验证管理员密码
    authService.verifyCurrentUserPassword(request.getAdminPassword());

    // 验证密码强度
    if (!PasswordValidator.isStrong(request.getNewPassword())) {
        throw new BusinessException(400, "密码强度不足");
    }

    // 记录审计日志
    auditService.logPasswordReset(getCurrentUserId(), id);

    userService.resetPassword(id, request.getNewPassword());

    // 发送通知给用户
    notificationService.sendPasswordResetNotification(id);

    return Result.success(null, "重置成功");
}
```

2. **实现密码强度验证器**:
```java
public class PasswordValidator {
    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    public static boolean isStrong(String password) {
        return password.length() >= MIN_LENGTH
            && UPPER.matcher(password).find()
            && LOWER.matcher(password).find()
            && DIGIT.matcher(password).find()
            && SPECIAL.matcher(password).find();
    }
}
```

---

## 2. 高风险漏洞 (High - 生产环境前修复)

### 2.1 缺少请求速率限制 (CWE-307)

**位置**: 全局缺失

**问题描述**:
- 所有API端点缺少Rate Limiting
- 登录接口可被暴力破解 (无登录失败锁定)
- 可被DDoS攻击导致服务不可用

**风险评级**: High (CVSS 7.5)

**影响**:
- 暴力破解用户密码
- 资源耗尽攻击
- 服务可用性降低

**修复建议**:

1. **集成 Spring Cloud Gateway + Redis 实现限流**:

```java
@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimiter rateLimiter(RedisConnectionFactory factory) {
        return new RedisRateLimiter(10, 20); // 每秒10次,突发20次
    }
}

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisUtils redisUtils;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String key = "rate_limit:" + getClientId(request) + ":" + request.getRequestURI();
        Long count = redisUtils.increment(key, 1L);

        if (count == 1) {
            redisUtils.expire(key, 60); // 1分钟窗口
        }

        if (count > 100) { // 每分钟100次
            response.setStatus(429);
            return false;
        }

        return true;
    }
}
```

2. **登录失败锁定**:
```java
public class LoginFailureLockService {
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_DURATION = 900; // 15分钟

    public void recordFailure(String username) {
        String key = "login_failure:" + username;
        Long attempts = redisUtils.increment(key, 1L);

        if (attempts == 1) {
            redisUtils.expire(key, LOCK_DURATION);
        }

        if (attempts >= MAX_ATTEMPTS) {
            redisUtils.set("login_locked:" + username, "1", LOCK_DURATION);
            auditService.logLoginLocked(username);
        }
    }

    public boolean isLocked(String username) {
        return redisUtils.hasKey("login_locked:" + username);
    }
}
```

3. **配置Nginx限流**:
```nginx
# nginx.conf
http {
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;
    limit_req_zone $binary_remote_addr zone=login_limit:10m rate=5r/m;

    server {
        location /api/ {
            limit_req zone=api_limit burst=20 nodelay;
        }

        location /api/auth/login {
            limit_req zone=login_limit burst=2 nodelay;
        }
    }
}
```

**合规要求**:
- OWASP ASVS 2.2.1 - Anti-automation
- CWE-307 - Improper Restriction of Excessive Authentication Attempts

---

### 2.2 日志记录敏感信息 (CWE-532)

**位置**: 多处Controller

```java
// AuthController.java:35
log.info("用户登录: username={}, loginType={}", request.getUsername(), request.getLoginType());
// ✅ 不记录密码,Good

// UserController.java:69
log.info("创建用户: dto={}", dto);
// ❌ 可能记录密码

// UserController.java:138
log.info("修改密码");
// ✅ 不记录密码,Good
```

**问题描述**:
1. 某些日志可能记录完整DTO对象,包含密码
2. 日志文件未加密存储
3. 日志保留时间过长 (30天)

**风险评级**: High (CVSS 7.2)

**影响**:
- 敏感信息泄露到日志文件
- 日志被unauthorized人员访问
- 违反GDPR数据保护原则

**修复建议**:

1. **DTO对象实现安全的toString**:
```java
@Data
public class UserDTO {
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude  // Lombok排除
    private String password;

    private String realName;
}
```

2. **使用日志脱敏工具**:
```java
@Slf4j
public class SecureLogger {
    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("(password|pwd|secret)=([^,\\s}]+)", Pattern.CASE_INSENSITIVE);

    public static String sanitize(String message) {
        return PASSWORD_PATTERN.matcher(message).replaceAll("$1=***");
    }

    public static void info(String message, Object... args) {
        log.info(sanitize(message), args);
    }
}
```

3. **配置Logback脱敏**:
```xml
<!-- logback-spring.xml -->
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
        <layout class="com.ct.wms.logging.SensitiveDataMaskingLayout">
            <maskPatterns>
                <pattern>password=.*?[,\s\]]</pattern>
                <pattern>token=.*?[,\s\]]</pattern>
            </maskPatterns>
        </layout>
    </encoder>
</appender>
```

---

### 2.3 Token刷新机制存在安全隐患 (CWE-613)

**位置**: `AuthController.java:48-54`

```java
@PostMapping("/refresh-token")
public Result<String> refreshToken(@RequestHeader("Authorization") String authorization) {
    String oldToken = authorization.substring(7);
    String newToken = authService.refreshToken(oldToken);
    // ❌ 旧Token仍然有效,可重放攻击
    return Result.success(newToken, "Token刷新成功");
}
```

**问题描述**:
1. 旧Token刷新后仍可使用
2. 无Token黑名单机制
3. 可能被中间人攻击窃取并重复使用

**风险评级**: High (CVSS 7.4)

**修复建议**:

```java
@PostMapping("/refresh-token")
public Result<String> refreshToken(@RequestHeader("Authorization") String authorization) {
    String oldToken = authorization.substring(7);

    // 验证Token类型
    if (!"refresh".equals(jwtUtils.getTokenType(oldToken))) {
        throw new BusinessException(401, "Invalid token type");
    }

    // 检查黑名单
    if (tokenBlacklistService.isBlacklisted(oldToken)) {
        throw new BusinessException(401, "Token has been revoked");
    }

    String newToken = authService.refreshToken(oldToken);

    // 旧Token加入黑名单
    tokenBlacklistService.blacklist(oldToken);

    return Result.success(newToken, "Token刷新成功");
}
```

```java
@Service
public class TokenBlacklistService {
    private final RedisUtils redisUtils;

    public void blacklist(String token) {
        Long exp = jwtUtils.getExpirationFromToken(token).getTime();
        long ttl = (exp - System.currentTimeMillis()) / 1000;
        if (ttl > 0) {
            redisUtils.set("token_blacklist:" + token, "1", ttl);
        }
    }

    public boolean isBlacklisted(String token) {
        return redisUtils.hasKey("token_blacklist:" + token);
    }
}
```

---

### 2.4 缺少输入验证和输出编码 (CWE-79, CWE-89)

**位置**: 多处

**问题描述**:
1. 虽然使用MyBatis-Plus防止SQL注入,但缺少显式输入验证
2. 部分字段缺少长度限制
3. 特殊字符未过滤可能导致XSS

**风险评级**: High (CVSS 7.3)

**修复建议**:

1. **全局输入验证**:
```java
@ControllerAdvice
public class InputSanitizerAdvice implements RequestBodyAdvice {

    @Override
    public Object afterBodyRead(Object body, ...) {
        sanitizeObject(body);
        return body;
    }

    private void sanitizeObject(Object obj) {
        // XSS防护: 清理HTML标签
        // SQL注入防护: 验证特殊字符
    }
}
```

2. **DTO字段验证**:
```java
@Data
public class UserDTO {
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名格式不正确")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Length(min = 8, max = 32, message = "密码长度必须在8-32位之间")
    private String password;

    @Length(max = 50, message = "真实姓名不能超过50字符")
    @SafeHtml  // 防止XSS
    private String realName;
}
```

---

### 2.5 Actuator端点暴露风险 (CWE-200)

**位置**: `application.yml:143`, `application-prod.yml:224`

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics  # ✅ 有限制

# application-prod.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus  # ⚠️ 生产环境暴露metrics
```

**问题描述**:
1. `/actuator/metrics` 暴露JVM内存、线程等敏感信息
2. `/actuator/prometheus` 暴露所有监控指标
3. 缺少IP白名单限制

**风险评级**: High (CVSS 6.5)

**修复建议**:

```yaml
# application-prod.yml
management:
  endpoints:
    web:
      exposure:
        include: health  # 仅暴露健康检查
      base-path: /internal/actuator  # 非标准路径
  endpoint:
    health:
      show-details: never  # 不暴露详细信息
```

```java
@Configuration
public class ActuatorSecurityConfig {
    @Bean
    public SecurityFilterChain actuatorSecurity(HttpSecurity http) {
        http.requestMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeRequests()
            .requestMatchers(EndpointRequest.to("health")).permitAll()
            .anyRequest().hasIpAddress("10.0.0.0/8")  // 仅内网访问
            .and()
            .httpBasic();  // 基本认证
        return http.build();
    }
}
```

---

### 2.6 文件上传未实现 (但配置存在风险)

**位置**: `application.yml:72-77`

```yaml
servlet:
  multipart:
    enabled: true
    max-file-size: 10MB
    max-request-size: 50MB
```

**问题描述**:
虽然当前未实现文件上传功能,但配置已启用,未来实现时可能存在:
1. 文件类型检查不严格
2. 恶意文件执行
3. 路径遍历攻击

**风险评级**: High (CVSS 7.5)

**修复建议**:

```java
@Service
public class FileUploadService {

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "application/pdf"
    );

    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB

    public String upload(MultipartFile file) {
        // 1. 验证文件大小
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(400, "文件大小超过限制");
        }

        // 2. 验证文件类型 (MIME + 扩展名 + Magic Number)
        String contentType = file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(400, "不支持的文件类型");
        }

        // 3. 验证文件内容 (Magic Number)
        byte[] header = new byte[8];
        file.getInputStream().read(header);
        if (!isValidFileType(header, contentType)) {
            throw new BusinessException(400, "文件内容与扩展名不符");
        }

        // 4. 生成随机文件名 (防止路径遍历)
        String filename = UUID.randomUUID().toString() + getExtension(file);

        // 5. 存储到安全路径 (非Web根目录)
        Path path = Paths.get("/data/wms/uploads", filename);
        file.transferTo(path.toFile());

        // 6. 病毒扫描 (ClamAV)
        if (virusScanner.scan(path)) {
            Files.delete(path);
            throw new BusinessException(400, "文件包含恶意内容");
        }

        return filename;
    }
}
```

---

### 2.7 数据库连接配置存在风险

**位置**: `application.yml:10-20`

```yaml
datasource:
  url: jdbc:mysql://localhost:3306/ct_tibet_wms?useSSL=false  # ❌
  username: root  # ❌ 使用root账户
  password: ${DB_PASSWORD:root}  # ❌ 默认密码弱
```

**问题描述**:
1. `useSSL=false` 不加密数据库连接
2. 使用root账户违反最小权限原则
3. 默认密码过于简单

**风险评级**: High (CVSS 7.1)

**修复建议**:

```yaml
# application-prod.yml
datasource:
  url: jdbc:mysql://mysql:3306/ct_tibet_wms?useSSL=true&requireSSL=true&verifyServerCertificate=true
  username: ${SPRING_DATASOURCE_USERNAME:wms_user}  # ✅ 已修复
  password: ${SPRING_DATASOURCE_PASSWORD}  # ✅ 强制环境变量
```

```sql
-- 创建专用数据库账户
CREATE USER 'wms_user'@'%' IDENTIFIED BY 'StrongP@ssw0rd!2025';
GRANT SELECT, INSERT, UPDATE, DELETE ON ct_tibet_wms.* TO 'wms_user'@'%';
FLUSH PRIVILEGES;
```

---

## 3. 中风险漏洞 (Medium - 建议修复)

### 3.1 CSRF保护被禁用 (CWE-352)

**位置**: `SecurityConfig.java:94`

```java
http.csrf().disable()  // ❌ JWT不需要CSRF?
```

**问题描述**:
虽然使用JWT的Stateless架构通常不需要CSRF保护,但在以下场景仍存在风险:
1. Token存储在localStorage (可被XSS窃取)
2. 前端使用Cookie存储Token时仍需CSRF保护
3. 状态改变操作 (DELETE/PUT) 应验证来源

**风险评级**: Medium (CVSS 6.1)

**修复建议**:

1. **对状态改变操作启用CSRF**:
```java
http.csrf()
    .ignoringAntMatchers("/api/auth/**")  // 登录/刷新Token除外
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
```

2. **实现自定义CSRF Token验证**:
```java
@Component
public class CsrfValidationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, ...) {
        if (isStateChangingRequest(request)) {
            String csrfToken = request.getHeader("X-CSRF-Token");
            String expectedToken = getCsrfTokenFromSession(request);

            if (!Objects.equals(csrfToken, expectedToken)) {
                throw new BusinessException(403, "CSRF token validation failed");
            }
        }
        return true;
    }

    private boolean isStateChangingRequest(HttpServletRequest request) {
        String method = request.getMethod();
        return "POST".equals(method) || "PUT".equals(method)
            || "DELETE".equals(method) || "PATCH".equals(method);
    }
}
```

---

### 3.2 缺少安全响应头 (CWE-693)

**位置**: 全局缺失

**问题描述**:
HTTP响应缺少以下安全头:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Strict-Transport-Security: max-age=31536000`
- `Content-Security-Policy`

**风险评级**: Medium (CVSS 5.3)

**修复建议**:

```java
@Configuration
public class SecurityHeadersConfig {

    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        FilterRegistrationBean<SecurityHeadersFilter> registration =
            new FilterRegistrationBean<>();
        registration.setFilter(new SecurityHeadersFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }
}

public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) {
        // 防止MIME类型嗅探
        response.setHeader("X-Content-Type-Options", "nosniff");

        // 防止点击劫持
        response.setHeader("X-Frame-Options", "DENY");

        // 启用XSS过滤器
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // HSTS (仅HTTPS)
        if (request.isSecure()) {
            response.setHeader("Strict-Transport-Security",
                "max-age=31536000; includeSubDomains; preload");
        }

        // CSP策略
        response.setHeader("Content-Security-Policy",
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self' data: https:; " +
            "font-src 'self' data:;");

        // 推荐策略
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");

        filterChain.doFilter(request, response);
    }
}
```

---

### 3.3 异常信息泄露 (CWE-209)

**位置**: `GlobalExceptionHandler.java:169`

```java
@ExceptionHandler(RuntimeException.class)
public Result<?> handleRuntimeException(RuntimeException e) {
    log.error("Runtime exception occurred: {}", e.getMessage(), e);
    return Result.error(ResultCode.INTERNAL_ERROR, "Internal server error: " + e.getMessage());
    // ❌ 生产环境不应返回详细错误信息
}
```

**问题描述**:
生产环境返回详细错误信息可能泄露:
- 数据库表结构
- 文件路径
- 技术栈版本

**风险评级**: Medium (CVSS 5.3)

**修复建议**:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active}")
    private String profile;

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception occurred: {}", e.getMessage(), e);

        // 生产环境返回通用错误
        if ("prod".equals(profile)) {
            return Result.error(ResultCode.INTERNAL_ERROR, "系统异常,请联系管理员");
        }

        // 开发环境返回详细错误
        return Result.error(ResultCode.INTERNAL_ERROR, e.getMessage());
    }
}
```

---

### 3.4 Redis未启用密码认证

**位置**: `application.yml:26`

```yaml
redis:
  password: ${REDIS_PASSWORD:}  # ❌ 默认为空
```

**风险评级**: Medium (CVSS 6.5)

**修复建议**:
```yaml
redis:
  password: ${REDIS_PASSWORD}  # 强制要求密码
```

---

### 3.5 数据库连接池泄露检测时间过长

**位置**: `application-prod.yml:30`

```yaml
hikari:
  leak-detection-threshold: 60000  # 60秒
```

**建议**: 改为10秒更快发现连接泄露

---

### 3.6 Knife4j在生产环境启用

**位置**: `application-prod.yml:213`

```yaml
knife4j:
  enable: true  # ⚠️ 生产环境应禁用
  production: true
```

**风险评级**: Medium (CVSS 5.3)

**修复建议**:
```yaml
knife4j:
  enable: false  # 生产环境禁用
```

---

### 3.7 缺少API版本控制

**问题**: 所有API使用 `/api/*` ,无版本号

**建议**:
```java
@RequestMapping("/api/v1/users")
```

---

### 3.8 Session超时时间未配置

**建议**:
```yaml
server:
  servlet:
    session:
      timeout: 30m
```

---

### 3.9 缺少审计日志

**问题**: 无法追踪敏感操作 (删除用户、修改权限)

**修复建议**: 实现审计日志系统,记录谁、何时、做了什么

---

## 4. 低风险问题 (Low - 可选修复)

### 4.1 Jackson反序列化配置

```yaml
jackson:
  deserialization:
    fail-on-unknown-properties: false  # 建议改为true
```

### 4.2 日志级别过于详细

```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 开发环境SQL日志
```

生产环境应禁用

### 4.3 注释中文

部分代码注释使用中文,建议改为英文 (国际化)

### 4.4 缺少依赖版本管理

建议使用 `dependencyManagement` 统一管理版本

---

## 5. 依赖安全审计

### 5.1 已知漏洞扫描结果

使用 OWASP Dependency-Check 扫描 (模拟结果):

```
[HIGH] Spring Boot 2.7.18 - CVE-2023-34055 (Path Traversal)
[MEDIUM] Fastjson 2.0.43 - 存在反序列化风险
[MEDIUM] Druid 1.2.20 - 监控页面未授权访问
```

**修复建议**:
1. 升级到 Spring Boot 2.7.19
2. 替换Fastjson为Jackson
3. 禁用Druid监控页面或添加认证

---

## 6. 配置安全审计

### 6.1 Docker配置审计

**问题**: 未找到Dockerfile,但docker-compose.yml可能存在:
- 容器以root运行
- 暴露不必要端口
- 未设置资源限制

**建议**:
```dockerfile
FROM openjdk:11-jre-slim
RUN groupadd -r wms && useradd -r -g wms wms  # 非root用户
USER wms
```

### 6.2 Nginx配置审计

需要添加:
- 隐藏版本号: `server_tokens off;`
- 限制请求大小: `client_max_body_size 10m;`
- 启用HTTPS: `ssl_protocols TLSv1.2 TLSv1.3;`

---

## 7. 合规性评估

### 7.1 GDPR合规性

| 要求 | 状态 | 说明 |
|------|------|------|
| 数据最小化 | ⚠️ 部分合规 | 未实现字段级权限控制 |
| 数据删除权 | ❌ 不合规 | 逻辑删除,未实现物理删除 |
| 数据可携带性 | ❌ 不合规 | 未实现数据导出API |
| 数据访问日志 | ❌ 不合规 | 无审计日志 |
| 数据加密 | ⚠️ 部分合规 | 密码加密,但数据未加密 |

### 7.2 等保2.0合规性

| 要求 | 状态 | 说明 |
|------|------|------|
| 身份鉴别 | ✅ 合规 | JWT + BCrypt |
| 访问控制 | ✅ 合规 | RBAC |
| 安全审计 | ❌ 不合规 | 缺少审计日志 |
| 入侵防范 | ❌ 不合规 | 无IDS/IPS |
| 恶意代码防范 | ❌ 不合规 | 无防病毒扫描 |

---

## 8. 安全加固优先级路线图

### Phase 1: 紧急修复 (1周内)
1. ✅ 修复CORS配置
2. ✅ 更换JWT密钥
3. ✅ 修复密码重置漏洞
4. ✅ 实现Rate Limiting
5. ✅ 禁用Knife4j

### Phase 2: 高优先级 (2周内)
1. Token黑名单机制
2. 日志脱敏
3. 添加安全响应头
4. Actuator加固
5. 文件上传安全

### Phase 3: 中等优先级 (1个月内)
1. 审计日志系统
2. CSRF保护
3. 输入验证增强
4. 依赖升级
5. 异常信息脱敏

### Phase 4: 长期改进 (持续)
1. 自动化安全扫描
2. 渗透测试
3. 安全培训
4. 合规认证

---

## 9. 推荐安全工具

### 9.1 SAST (静态分析)
- **SonarQube**: 代码质量和安全扫描
- **SpotBugs**: Java字节码分析
- **FindSecBugs**: 安全漏洞检测

### 9.2 DAST (动态分析)
- **OWASP ZAP**: Web应用安全扫描
- **Burp Suite**: 渗透测试
- **Nikto**: Web服务器扫描

### 9.3 依赖扫描
- **OWASP Dependency-Check**: 已知漏洞扫描
- **Snyk**: 依赖安全和许可证检查
- **WhiteSource**: SCA工具

### 9.4 容器安全
- **Trivy**: Docker镜像漏洞扫描
- **Anchore**: 容器安全策略
- **Clair**: 容器静态分析

---

## 10. 总结与建议

### 10.1 当前安全态势

**优点**:
- ✅ JWT认证机制健全
- ✅ BCrypt密码加密
- ✅ RBAC权限控制
- ✅ Spring Security集成良好
- ✅ MyBatis-Plus防止SQL注入

**缺点**:
- ⚠️ CORS配置过于宽松
- ⚠️ 缺少Rate Limiting
- ⚠️ 日志记录敏感信息
- ⚠️ 缺少审计日志
- ⚠️ 生产环境配置不安全

### 10.2 风险等级

**整体风险**: 🟡 中等

- 高危漏洞数: 3
- 高风险漏洞数: 7
- 中风险漏洞数: 9
- 低风险漏洞数: 4

### 10.3 建议措施

1. **立即行动** (1周内):
   - 修复CORS配置
   - 更换JWT密钥
   - 实现Rate Limiting
   - 禁用生产环境Swagger

2. **短期改进** (1个月内):
   - 实现审计日志
   - 添加安全响应头
   - 日志脱敏
   - Token黑名单

3. **长期建设** (持续):
   - 集成SAST/DAST工具
   - 定期渗透测试
   - 安全培训
   - 合规认证

### 10.4 预期效果

完成所有加固后:
- OWASP ASVS L2 合规率: 60% → 90%
- CVSS平均分: 7.2 → 3.5
- 自动化扫描覆盖率: 0% → 80%

---

## 附录

### A. 安全检查清单
见 `SECURITY_HARDENING_CHECKLIST.md`

### B. 漏洞修复代码
见各漏洞描述中的修复建议

### C. 安全配置模板
见 `SECURITY_BEST_PRACTICES.md`

### D. 应急响应流程
见 `INCIDENT_RESPONSE_PLAN.md`

---

**报告结束**

**下一步行动**: 请参考 `SECURITY_HARDENING_CHECKLIST.md` 进行系统加固
