#!/usr/bin/env python3
"""
批量修复前端分页数据格式问题
将 res.data.list 替换为 res.data
将 res.data.total 替换为 res.total
"""

import os
import re

# 需要修复的文件列表
files_to_fix = [
    "frontend-pc/src/views/apply/list/index.vue",
    "frontend-pc/src/views/approval/approved/index.vue",
    "frontend-pc/src/views/approval/pending/index.vue",
    "frontend-pc/src/views/basic/user/index.vue",
    "frontend-pc/src/views/basic/warehouse/index.vue",
    "frontend-pc/src/views/inbound/list/index.vue",
    "frontend-pc/src/views/inventory/query/index.vue",
    "frontend-pc/src/views/inventory/warning/index.vue",
    "frontend-pc/src/views/message/list/index.vue",
    "frontend-pc/src/views/outbound/confirm/index.vue",
    "frontend-pc/src/views/outbound/list/index.vue",
]

def fix_file(filepath):
    """修复单个文件"""
    if not os.path.exists(filepath):
        print(f"跳过: {filepath} (文件不存在)")
        return False

    try:
        # 读取文件
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        # 备份原内容
        original_content = content

        # 替换
        content = re.sub(r'res\.data\.list', 'res.data', content)
        content = re.sub(r'res\.data\.total', 'res.total', content)

        # 如果有变化，写入文件
        if content != original_content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"[OK] Fixed: {filepath}")
            return True
        else:
            print(f"[SKIP] No changes needed: {filepath}")
            return False

    except Exception as e:
        print(f"[ERROR] {filepath} - {str(e)}")
        return False

def main():
    print("=" * 60)
    print("批量修复前端分页数据格式")
    print("=" * 60)
    print()

    fixed_count = 0
    for filepath in files_to_fix:
        if fix_file(filepath):
            fixed_count += 1

    print()
    print("=" * 60)
    print(f"修复完成！共修复 {fixed_count} 个文件")
    print("=" * 60)

if __name__ == "__main__":
    main()
