#!/usr/bin/env python3
"""
生成 TabBar 占位符图标
需要安装依赖: pip install Pillow
运行: python scripts/generate-tabbar-icons.py
"""

import os
from pathlib import Path

try:
    from PIL import Image, ImageDraw, ImageFont
except ImportError:
    print("❌ 错误: 未安装 Pillow 模块")
    print("📦 请先安装依赖: pip install Pillow")
    exit(1)

# 输出目录
OUTPUT_DIR = Path(__file__).parent.parent / 'static' / 'tabbar'

# 图标配置
ICONS = [
    {'name': 'home', 'text': '🏠', 'description': '首页'},
    {'name': 'apply', 'text': '📝', 'description': '申请'},
    {'name': 'inventory', 'text': '📦', 'description': '库存'},
    {'name': 'mine', 'text': '👤', 'description': '我的'},
    {'name': 'approval', 'text': '✓', 'description': '审批'},
    {'name': 'quick', 'text': '⚡', 'description': '快捷'},
]

# 颜色配置
COLORS = {
    'inactive': '#8c8c8c',
    'active': '#1890ff',
    'bg_inactive': '#f5f5f5',
    'bg_active': '#e6f7ff'
}

def hex_to_rgb(hex_color):
    """将十六进制颜色转换为 RGB"""
    hex_color = hex_color.lstrip('#')
    return tuple(int(hex_color[i:i+2], 16) for i in (0, 2, 4))

def generate_icon(config, is_active, size=81):
    """生成图标"""
    # 创建画布
    image = Image.new('RGBA', (size, size), (255, 255, 255, 0))
    draw = ImageDraw.Draw(image)

    # 设置颜色
    bg_color = COLORS['bg_active'] if is_active else COLORS['bg_inactive']
    text_color = COLORS['active'] if is_active else COLORS['inactive']

    # 绘制圆形背景
    margin = 5
    draw.ellipse(
        [margin, margin, size - margin, size - margin],
        fill=hex_to_rgb(bg_color)
    )

    # 尝试加载字体（Emoji 支持）
    try:
        # Windows 系统的 Emoji 字体
        font = ImageFont.truetype("seguiemj.ttf", 40)
    except:
        try:
            # macOS 系统的 Emoji 字体
            font = ImageFont.truetype("/System/Library/Fonts/Apple Color Emoji.ttc", 40)
        except:
            # 使用默认字体
            font = ImageFont.load_default()

    # 绘制文字
    bbox = draw.textbbox((0, 0), config['text'], font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]
    position = ((size - text_width) // 2, (size - text_height) // 2 - 5)

    draw.text(position, config['text'], fill=hex_to_rgb(text_color), font=font)

    return image

def main():
    """主函数"""
    print("🎨 开始生成 TabBar 图标...\n")

    # 确保输出目录存在
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    success_count = 0
    total_count = len(ICONS) * 2

    for config in ICONS:
        try:
            # 生成未选中状态图标
            inactive_image = generate_icon(config, False)
            inactive_path = OUTPUT_DIR / f"{config['name']}.png"
            inactive_image.save(inactive_path)
            print(f"✓ 生成 {config['description']} (未选中): {inactive_path.name}")
            success_count += 1

            # 生成选中状态图标
            active_image = generate_icon(config, True)
            active_path = OUTPUT_DIR / f"{config['name']}-active.png"
            active_image.save(active_path)
            print(f"✓ 生成 {config['description']} (选中): {active_path.name}")
            success_count += 1

        except Exception as e:
            print(f"✗ 生成 {config['description']} 失败: {str(e)}")

    print(f"\n✨ 完成! 成功生成 {success_count}/{total_count} 个图标")
    print(f"📁 输出目录: {OUTPUT_DIR}")

    if success_count == total_count:
        print("\n💡 提示: 现在可以在 pages.json 中启用图标路径了")

if __name__ == '__main__':
    main()
