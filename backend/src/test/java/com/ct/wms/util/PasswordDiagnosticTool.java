package com.ct.wms.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码诊断工具
 * 用于诊断和测试密码验证问题
 */
public class PasswordDiagnosticTool {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("======================================");
        System.out.println("密码诊断工具");
        System.out.println("======================================");
        System.out.println();

        // 测试密码
        String testPassword = "123456";

        // 数据库中可能的hash值
        String[] possibleHashes = {
            // 旧的（可能错误的）hash
            "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH",
            // 新生成的正确hash
            "$2a$10$j7sCAk4BYfqNqmnW.7QK7eyNjtt01JOcehbxpjg5T7ImShI0751.e"
        };

        System.out.println("测试密码: " + testPassword);
        System.out.println();
        System.out.println("======================================");
        System.out.println("验证测试");
        System.out.println("======================================");

        for (int i = 0; i < possibleHashes.length; i++) {
            String hash = possibleHashes[i];
            boolean matches = encoder.matches(testPassword, hash);

            System.out.println("\nHash " + (i + 1) + ":");
            System.out.println("值: " + hash);
            System.out.println("验证结果: " + (matches ? "✓ 通过" : "✗ 失败"));

            if (i == 0) {
                System.out.println("说明: 旧的SQL脚本中的hash");
            } else if (i == 1) {
                System.out.println("说明: 新生成的正确hash");
            }
        }

        System.out.println("\n======================================");
        System.out.println("诊断结论");
        System.out.println("======================================");

        boolean oldHashWorks = encoder.matches(testPassword, possibleHashes[0]);
        boolean newHashWorks = encoder.matches(testPassword, possibleHashes[1]);

        if (oldHashWorks && newHashWorks) {
            System.out.println("✓ 两个hash都有效");
            System.out.println("建议: 使用新hash确保兼容性");
        } else if (!oldHashWorks && newHashWorks) {
            System.out.println("✗ 旧hash无效，新hash有效");
            System.out.println("问题: 数据库中可能还在使用旧hash");
            System.out.println("解决: 需要更新数据库密码");
        } else if (oldHashWorks && !newHashWorks) {
            System.out.println("✓ 旧hash有效，新hash无效");
            System.out.println("问题: 新生成的hash有问题");
        } else {
            System.out.println("✗ 两个hash都无效");
            System.out.println("问题: 密码hash生成有严重问题");
        }

        System.out.println("\n======================================");
        System.out.println("生成新的有效hash");
        System.out.println("======================================");

        // 生成5个新的hash用于测试
        System.out.println("\n为密码 '" + testPassword + "' 生成新hash:");
        for (int i = 1; i <= 5; i++) {
            String newHash = encoder.encode(testPassword);
            boolean verify = encoder.matches(testPassword, newHash);
            System.out.println("\n生成 " + i + ":");
            System.out.println("Hash: " + newHash);
            System.out.println("验证: " + (verify ? "✓" : "✗"));
        }

        System.out.println("\n======================================");
        System.out.println("如何修复");
        System.out.println("======================================");
        System.out.println("1. 复制上面任意一个通过验证的hash");
        System.out.println("2. 执行以下SQL:");
        System.out.println("   UPDATE tb_user SET password = '<复制的hash>' WHERE username = 'admin';");
        System.out.println("3. 或运行修复脚本: sql/fix_password.bat");
        System.out.println("======================================");
    }
}
