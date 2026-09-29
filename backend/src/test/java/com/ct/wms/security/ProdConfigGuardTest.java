package com.ct.wms.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 生产配置守卫（W4 OWASP 自查）：生产环境需要 MySQL/Redis，没法在单测里整体启动，
 * 所以直接读 application-prod.yml，锁住几项安全相关的配置，防止以后被改回去。
 */
@DisplayName("生产配置守卫")
class ProdConfigGuardTest {

    private static Properties prod() {
        YamlPropertiesFactoryBean y = new YamlPropertiesFactoryBean();
        y.setResources(new ClassPathResource("application-prod.yml"));
        return y.getObject();
    }

    @Test
    @DisplayName("A05：生产关闭 springdoc 接口文档（它在 SecurityConfig 里是匿名放行的）")
    void apiDocsDisabledInProd() {
        Properties p = prod();
        assertThat(p.getProperty("springdoc.api-docs.enabled")).isEqualTo("false");
        assertThat(p.getProperty("springdoc.swagger-ui.enabled")).isEqualTo("false");
        assertThat(p.getProperty("knife4j.enable")).isEqualTo("false");
    }

    @Test
    @DisplayName("A02/A05：密钥与跨域来源只从环境变量读取，没有写死的默认值")
    void secretsComeFromEnvironment() {
        Properties p = prod();
        assertThat(p.getProperty("jwt.secret")).isEqualTo("${JWT_SECRET}");
        assertThat(p.getProperty("spring.datasource.password")).startsWith("${").doesNotContain(":");
    }
}
