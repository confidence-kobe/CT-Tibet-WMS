package com.ct.wms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 仓库管理系统启动类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@SpringBootApplication
@MapperScan("com.ct.wms.**.mapper")
@EnableAsync
@EnableScheduling
public class WmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsApplication.class, args);
        System.out.println("\n" +
                "==========================================\n" +
                "   西藏电信仓库管理系统启动成功!\n" +
                "   CT Tibet WMS Started Successfully!\n" +
                "   API文档: http://localhost:8080/doc.html\n" +
                "==========================================\n");
    }
}
