#!/bin/bash

# 测试物资API接口

echo "======================================="
echo "测试物资列表API"
echo "======================================="

# 1. 登录获取Token
echo "1. 登录获取Token..."
LOGIN_RESPONSE=$(curl -s -X POST "http://localhost:48888/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}')

echo "$LOGIN_RESPONSE" | python -m json.tool

TOKEN=$(echo "$LOGIN_RESPONSE" | python -c "import sys, json; print(json.load(sys.stdin)['data']['token'])" 2>/dev/null)

if [ -z "$TOKEN" ]; then
    echo "❌ 登录失败，无法获取Token"
    exit 1
fi

echo ""
echo "✓ 登录成功"
echo "Token: ${TOKEN:0:50}..."
echo ""

# 2. 测试物资列表API
echo "2. 测试物资列表API..."
MATERIAL_RESPONSE=$(curl -s -X GET "http://localhost:48888/api/materials?pageNum=1&pageSize=20" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN")

echo "$MATERIAL_RESPONSE" | python -m json.tool

echo ""
echo "======================================="
echo "测试完成"
echo "======================================="
