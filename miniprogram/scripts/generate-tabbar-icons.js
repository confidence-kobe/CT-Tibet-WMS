/**
 * 生成 TabBar 占位符图标
 * 需要安装依赖: npm install canvas
 * 运行: node scripts/generate-tabbar-icons.js
 */

const fs = require('fs');
const path = require('path');

// 检查是否安装了 canvas 模块
let Canvas;
try {
  Canvas = require('canvas');
} catch (err) {
  console.error('❌ 错误: 未安装 canvas 模块');
  console.log('📦 请先安装依赖: npm install canvas');
  console.log('⚠️  注意: canvas 模块需要系统依赖，Windows 用户可能需要安装额外的构建工具');
  console.log('💡 替代方案: 使用在线工具或设计软件手动创建图标');
  process.exit(1);
}

const { createCanvas } = Canvas;

// 输出目录
const outputDir = path.join(__dirname, '../static/tabbar');

// 确保输出目录存在
if (!fs.existsSync(outputDir)) {
  fs.mkdirSync(outputDir, { recursive: true });
}

// 图标配置
const icons = [
  {
    name: 'home',
    text: '🏠',
    description: '首页'
  },
  {
    name: 'apply',
    text: '📝',
    description: '申请'
  },
  {
    name: 'inventory',
    text: '📦',
    description: '库存'
  },
  {
    name: 'mine',
    text: '👤',
    description: '我的'
  },
  {
    name: 'approval',
    text: '✓',
    description: '审批'
  },
  {
    name: 'quick',
    text: '⚡',
    description: '快捷'
  }
];

// 颜色配置
const colors = {
  inactive: '#8c8c8c',
  active: '#1890ff'
};

/**
 * 生成图标
 */
function generateIcon(config, isActive) {
  const size = 81;
  const canvas = createCanvas(size, size);
  const ctx = canvas.getContext('2d');

  // 设置颜色
  const color = isActive ? colors.active : colors.inactive;

  // 绘制背景（透明）
  ctx.clearRect(0, 0, size, size);

  // 绘制圆形背景
  ctx.beginPath();
  ctx.arc(size / 2, size / 2, size / 2 - 5, 0, Math.PI * 2);
  ctx.fillStyle = isActive ? '#e6f7ff' : '#f5f5f5';
  ctx.fill();

  // 绘制文字/图标
  ctx.font = 'bold 40px Arial';
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.fillStyle = color;
  ctx.fillText(config.text, size / 2, size / 2);

  return canvas;
}

/**
 * 保存图标
 */
function saveIcon(canvas, filename) {
  const buffer = canvas.toBuffer('image/png');
  const filepath = path.join(outputDir, filename);
  fs.writeFileSync(filepath, buffer);
  return filepath;
}

/**
 * 主函数
 */
function main() {
  console.log('🎨 开始生成 TabBar 图标...\n');

  let successCount = 0;
  let totalCount = icons.length * 2;

  icons.forEach(config => {
    try {
      // 生成未选中状态图标
      const inactiveCanvas = generateIcon(config, false);
      const inactivePath = saveIcon(inactiveCanvas, `${config.name}.png`);
      console.log(`✓ 生成 ${config.description} (未选中): ${path.basename(inactivePath)}`);
      successCount++;

      // 生成选中状态图标
      const activeCanvas = generateIcon(config, true);
      const activePath = saveIcon(activeCanvas, `${config.name}-active.png`);
      console.log(`✓ 生成 ${config.description} (选中): ${path.basename(activePath)}`);
      successCount++;
    } catch (err) {
      console.error(`✗ 生成 ${config.description} 失败:`, err.message);
    }
  });

  console.log(`\n✨ 完成! 成功生成 ${successCount}/${totalCount} 个图标`);
  console.log(`📁 输出目录: ${outputDir}`);

  if (successCount === totalCount) {
    console.log('\n💡 提示: 现在可以在 pages.json 中启用图标路径了');
  }
}

// 运行
main();
