-- 直接修复密码
USE ct_tibet_wms;

-- 更新所有用户密码为正确的BCrypt hash (密码: 123456)
UPDATE tb_user
SET password = '$2a$10$j7sCAk4BYfqNqmnW.7QK7eyNjtt01JOcehbxpjg5T7ImShI0751.e';

-- 显示更新结果
SELECT
    id,
    username,
    real_name,
    '123456' AS password_plaintext,
    'Updated' AS status
FROM tb_user
ORDER BY id;

SELECT '======================================' AS '';
SELECT '密码已成功更新！' AS '';
SELECT '所有账号密码: 123456' AS '';
SELECT '======================================' AS '';
