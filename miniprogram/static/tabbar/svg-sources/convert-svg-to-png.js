/**
 * SVG转PNG批量转换脚本
 *
 * 功能:
 * - 将当前目录的所有SVG文件转换为PNG
 * - 自动生成2个颜色版本（灰色和紫色）
 * - 自动命名为规范格式
 *
 * 依赖:
 * npm install sharp
 *
 * 使用:
 * node convert-svg-to-png.js
 */

const fs = require('fs');
const path = require('path');
const sharp = require('sharp');

// 配置
const CONFIG = {
  size: 81,
  colorGray: '#999999',
  colorPurple: '#7C3AED',
  inputDir: __dirname,
  outputDir: path.join(__dirname, '..')
};

// SVG文件列表
const svgFiles = [
  'home',
  'apply',
  'approval',
  'inventory',
  'message',
  'mine',
  'warehouse'
];

/**
 * 替换SVG中的颜色
 */
function replaceSvgColor(svgContent, color) {
  return svgContent.replace(/currentColor/g, color);
}

/**
 * 转换单个SVG为PNG
 */
async function convertSvgToPng(svgName, color, outputName) {
  try {
    const svgPath = path.join(CONFIG.inputDir, `${svgName}.svg`);
    const outputPath = path.join(CONFIG.outputDir, outputName);

    // 读取SVG内容
    let svgContent = fs.readFileSync(svgPath, 'utf8');

    // 替换颜色
    svgContent = replaceSvgColor(svgContent, color);

    // 转换为PNG
    await sharp(Buffer.from(svgContent))
      .resize(CONFIG.size, CONFIG.size)
      .png()
      .toFile(outputPath);

    console.log(`✓ 已生成: ${outputName}`);
    return true;
  } catch (error) {
    console.error(`✗ 转换失败: ${outputName}`, error.message);
    return false;
  }
}

/**
 * 批量转换
 */
async function convertAll() {
  console.log('开始转换SVG为PNG...\n');
  console.log(`输入目录: ${CONFIG.inputDir}`);
  console.log(`输出目录: ${CONFIG.outputDir}`);
  console.log(`图标尺寸: ${CONFIG.size}×${CONFIG.size}px`);
  console.log(`颜色配置: 灰色=${CONFIG.colorGray}, 紫色=${CONFIG.colorPurple}\n`);

  let successCount = 0;
  let failCount = 0;

  for (const svgName of svgFiles) {
    console.log(`处理: ${svgName}.svg`);

    // 生成灰色版本（未选中）
    const grayResult = await convertSvgToPng(
      svgName,
      CONFIG.colorGray,
      `${svgName}.png`
    );

    // 生成紫色版本（选中）
    const purpleResult = await convertSvgToPng(
      svgName,
      CONFIG.colorPurple,
      `${svgName}-active.png`
    );

    if (grayResult && purpleResult) {
      successCount += 2;
    } else {
      failCount += 2;
    }

    console.log('');
  }

  console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
  console.log(`转换完成!`);
  console.log(`成功: ${successCount}个文件`);
  console.log(`失败: ${failCount}个文件`);
  console.log(`输出目录: ${CONFIG.outputDir}`);
  console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━');

  if (successCount > 0) {
    console.log('\n下一步:');
    console.log('1. 检查生成的PNG文件');
    console.log('2. 使用TinyPNG压缩 (https://tinypng.com/)');
    console.log('3. 在微信开发者工具中测试显示效果');
  }
}

// 检查依赖
try {
  require.resolve('sharp');
  convertAll().catch(console.error);
} catch (e) {
  console.error('错误: 未安装sharp库');
  console.error('请先运行: npm install sharp');
  console.error('\n或者使用在线工具转换SVG:');
  console.error('- https://www.photopea.com/');
  console.error('- https://svgtopng.com/');
}
