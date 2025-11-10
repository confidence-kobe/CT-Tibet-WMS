package com.ct.wms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API文档配置
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("西藏电信仓库管理系统 API")
                        .description("CT-Tibet-WMS API Documentation")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("CT Development Team")
                                .email("dev@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0")));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("1. 认证授权")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("2. 用户管理")
                .pathsToMatch("/api/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi materialApi() {
        return GroupedOpenApi.builder()
                .group("3. 物资管理")
                .pathsToMatch("/api/materials/**")
                .build();
    }

    @Bean
    public GroupedOpenApi warehouseApi() {
        return GroupedOpenApi.builder()
                .group("4. 仓库管理")
                .pathsToMatch("/api/warehouses/**")
                .build();
    }

    @Bean
    public GroupedOpenApi inboundApi() {
        return GroupedOpenApi.builder()
                .group("5. 入库管理")
                .pathsToMatch("/api/inbounds/**")
                .build();
    }

    @Bean
    public GroupedOpenApi outboundApi() {
        return GroupedOpenApi.builder()
                .group("6. 出库管理")
                .pathsToMatch("/api/outbounds/**")
                .build();
    }

    @Bean
    public GroupedOpenApi applyApi() {
        return GroupedOpenApi.builder()
                .group("7. 申请审批")
                .pathsToMatch("/api/applies/**")
                .build();
    }

    @Bean
    public GroupedOpenApi inventoryApi() {
        return GroupedOpenApi.builder()
                .group("8. 库存管理")
                .pathsToMatch("/api/inventory/**")
                .build();
    }

    @Bean
    public GroupedOpenApi statsApi() {
        return GroupedOpenApi.builder()
                .group("9. 统计报表")
                .pathsToMatch("/api/stats/**")
                .build();
    }

    @Bean
    public GroupedOpenApi messageApi() {
        return GroupedOpenApi.builder()
                .group("10. 消息通知")
                .pathsToMatch("/api/messages/**")
                .build();
    }

    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("11. 系统管理")
                .pathsToMatch("/api/depts/**", "/api/roles/**", "/api/menus/**")
                .build();
    }
}
