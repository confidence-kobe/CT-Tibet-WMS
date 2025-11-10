#!/bin/bash

# =============================================
# 西藏电信仓库管理系统 - 数据库安装脚本（Linux/Mac）
# =============================================

echo "============================================="
echo "西藏电信仓库管理系统 - 数据库安装脚本"
echo "============================================="
echo ""

read -sp "请输入MySQL root密码: " MYSQL_PASSWORD
echo ""

echo ""
echo "[1/3] 正在创建数据库..."
mysql -u root -p"${MYSQL_PASSWORD}" < create_database.sql
if [ $? -ne 0 ]; then
    echo "❌ 创建数据库失败！"
    exit 1
fi
echo "✓ 数据库创建成功"

echo ""
echo "[2/3] 正在创建表结构..."
mysql -u root -p"${MYSQL_PASSWORD}" ct_tibet_wms < schema.sql
if [ $? -ne 0 ]; then
    echo "❌ 创建表结构失败！"
    exit 1
fi
echo "✓ 表结构创建成功"

echo ""
echo "[3/3] 正在初始化数据..."
mysql -u root -p"${MYSQL_PASSWORD}" ct_tibet_wms < init_data.sql
if [ $? -ne 0 ]; then
    echo "❌ 初始化数据失败！"
    exit 1
fi
echo "✓ 数据初始化成功"

echo ""
echo "============================================="
echo "✓ 数据库安装完成！"
echo "============================================="
echo ""
echo "初始账号信息:"
echo "  管理员账号: admin / 123456"
echo "  仓管账号: wl_warehouse / 123456"
echo "  员工账号: wl_user1 / 123456"
echo ""
echo "数据库信息:"
echo "  数据库名: ct_tibet_wms"
echo "  字符集: utf8mb4"
echo "  表数量: 17张"
echo ""
