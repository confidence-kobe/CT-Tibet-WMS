-- =============================================
-- 密码修复脚本
-- =============================================
-- 用途: 将所有用户密码重置为 123456
-- 使用: mysql -u root -p ct_tibet_wms < fix_password.sql
-- =============================================

-- 正确的BCrypt密码hash (密码: 123456)
SET @correct_hash = '$2a$10$j7sCAk4BYfqNqmnW.7QK7eyNjtt01JOcehbxpjg5T7ImShI0751.e';

-- 更新所有用户密码
UPDATE tb_user SET password = @correct_hash;

-- 验证更新结果
SELECT
    id,
    username,
    real_name,
    CASE
        WHEN password = @correct_hash THEN '✓ 密码已更新'
        ELSE '✗ 更新失败'
    END AS status
FROM tb_user
ORDER BY id;

-- 输出提示
SELECT '======================================' AS '';
SELECT '密码修复完成!' AS '';
SELECT '所有账号密码已重置为: 123456' AS '';
SELECT '======================================' AS '';
