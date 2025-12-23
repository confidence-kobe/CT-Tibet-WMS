package com.ct.wms.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码Hash生成工具
 * 用于生成BCrypt加密的密码hash值
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 生成密码：123456
        String password = "123456";
        String hash1 = encoder.encode(password);
        String hash2 = encoder.encode(password);
        String hash3 = encoder.encode(password);

        System.out.println("======================================");
        System.out.println("BCrypt 密码Hash生成器");
        System.out.println("======================================");
        System.out.println("原始密码: " + password);
        System.out.println("--------------------------------------");
        System.out.println("生成的Hash值（每次不同）:");
        System.out.println("Hash 1: " + hash1);
        System.out.println("Hash 2: " + hash2);
        System.out.println("Hash 3: " + hash3);
        System.out.println("--------------------------------------");

        // 验证所有hash都能正确匹配原始密码
        System.out.println("验证测试:");
        System.out.println("Hash 1 matches: " + encoder.matches(password, hash1));
        System.out.println("Hash 2 matches: " + encoder.matches(password, hash2));
        System.out.println("Hash 3 matches: " + encoder.matches(password, hash3));
        System.out.println("--------------------------------------");

        // 测试错误密码
        System.out.println("错误密码测试:");
        System.out.println("'wrong_password' matches: " + encoder.matches("wrong_password", hash1));
        System.out.println("======================================");

        // 生成其他常用密码
        System.out.println("\n其他常用密码的Hash值:");
        System.out.println("--------------------------------------");

        String[] passwords = {"admin123", "password", "111111", "654321"};
        for (String pwd : passwords) {
            String hash = encoder.encode(pwd);
            System.out.println("密码 '" + pwd + "':");
            System.out.println("Hash: " + hash);
            System.out.println("验证: " + encoder.matches(pwd, hash));
            System.out.println();
        }

        System.out.println("======================================");
        System.out.println("使用方法:");
        System.out.println("1. 复制上面生成的Hash值");
        System.out.println("2. 更新SQL脚本中的password字段");
        System.out.println("3. 重新执行数据库初始化");
        System.out.println("======================================");
    }
}
