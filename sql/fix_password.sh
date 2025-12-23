#!/bin/bash

# =============================================
# 密码修复脚本 (Linux/Mac)
# =============================================

echo "======================================"
echo "密码修复脚本"
echo "======================================"
echo ""
echo "此脚本将重置所有用户密码为: 123456"
echo ""

read -p "确认执行? (y/n): " confirm
if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
    echo "操作已取消"
    exit 0
fi

echo ""
echo "请输入MySQL连接信息:"
read -p "MySQL主机 (默认: localhost): " MYSQL_HOST
MYSQL_HOST=${MYSQL_HOST:-localhost}

read -p "MySQL端口 (默认: 3306): " MYSQL_PORT
MYSQL_PORT=${MYSQL_PORT:-3306}

read -p "MySQL用户 (默认: root): " MYSQL_USER
MYSQL_USER=${MYSQL_USER:-root}

read -sp "MySQL密码: " MYSQL_PASSWORD
echo ""

echo ""
echo "正在执行密码修复..."
mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" ct_tibet_wms < fix_password.sql

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ 密码修复成功！"
    echo ""
    echo "现在可以使用以下账号登录:"
    echo "  admin / 123456"
    echo "  wl_warehouse / 123456"
    echo "  wl_user1 / 123456"
else
    echo ""
    echo "✗ 密码修复失败！"
    echo "请检查:"
    echo "  1. MySQL服务是否运行"
    echo "  2. 连接信息是否正确"
    echo "  3. 数据库 ct_tibet_wms 是否存在"
fi

echo ""
