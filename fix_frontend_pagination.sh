#!/bin/bash

# 批量修复前端分页数据格式问题
# 问题: 前端使用 res.data.list 和 res.data.total
# 实际: 后端返回 res.data (数组) 和 res.total

echo "======================================="
echo "批量修复前端分页数据格式"
echo "======================================="

# 需要修复的文件列表
FILES=(
  "frontend-pc/src/views/apply/list/index.vue"
  "frontend-pc/src/views/approval/approved/index.vue"
  "frontend-pc/src/views/approval/pending/index.vue"
  "frontend-pc/src/views/basic/user/index.vue"
  "frontend-pc/src/views/basic/warehouse/index.vue"
  "frontend-pc/src/views/inbound/list/index.vue"
  "frontend-pc/src/views/inventory/query/index.vue"
  "frontend-pc/src/views/inventory/warning/index.vue"
  "frontend-pc/src/views/message/list/index.vue"
  "frontend-pc/src/views/outbound/confirm/index.vue"
  "frontend-pc/src/views/outbound/list/index.vue"
)

COUNT=0

for file in "${FILES[@]}"; do
  if [ -f "$file" ]; then
    echo "修复: $file"

    # 备份原文件
    cp "$file" "${file}.bak"

    # 替换 res.data.list 为 res.data
    # 替换 res.data.total 为 res.total
    sed -i 's/res\.data\.list/res.data/g' "$file"
    sed -i 's/res\.data\.total/res.total/g' "$file"

    COUNT=$((COUNT + 1))
  else
    echo "跳过: $file (文件不存在)"
  fi
done

echo ""
echo "======================================="
echo "修复完成!"
echo "======================================="
echo "修复文件数: $COUNT"
echo "备份文件: *.vue.bak"
echo ""
echo "如需回滚，执行:"
echo "  for f in frontend-pc/src/views/**/*.vue.bak; do mv \"\$f\" \"\${f%.bak}\"; done"
echo "======================================="
