# 🚀 快速启动小程序

## 方式一：使用 HBuilderX（推荐）

### 步骤 1：打开项目

1. 启动 HBuilderX
2. 文件 → 打开目录
3. 选择 `H:\java\CT-Tibet-WMS\miniprogram` 文件夹

### 步骤 2：配置 AppID

**方法 A：使用测试号（推荐）**

不需要修改配置，直接进行步骤 3。在微信开发者工具中选择"使用测试号"即可。

**方法 B：使用正式 AppID**

编辑 `manifest.json`，找到第 29 行：
```json
"mp-weixin": {
  "appid": "你的AppID",  // 修改这里
```

### 步骤 3：运行项目

1. 点击工具栏 → **运行**
2. 选择 → **运行到小程序模拟器**
3. 选择 → **微信开发者工具**
4. 等待编译完成（首次需要 1-2 分钟）

### 步骤 4：在微信开发者工具中预览

微信开发者工具会自动打开，显示项目：

**首次运行设置：**
1. 点击右上角 "详情"
2. 本地设置：
   - ✅ 不校验合法域名
   - ✅ 不校验 TLS 版本
   - ✅ 启用调试
3. 关闭详情面板

**预览效果：**
- 左侧：模拟器预览
- 中间：代码编辑器
- 右侧：调试控制台

---

## 方式二：使用命令行

### 步骤 1：安装依赖

```bash
cd H:\java\CT-Tibet-WMS\miniprogram
npm install
```

### 步骤 2：运行开发服务

```bash
npm run dev:mp-weixin
```

### 步骤 3：打开微信开发者工具

1. 打开微信开发者工具
2. 点击 "+" → 导入项目
3. 选择目录：`H:\java\CT-Tibet-WMS\miniprogram\dist\dev\mp-weixin`
4. 填写 AppID（或选择测试号）
5. 点击"导入"

---

## 预览页面

### 登录页面

由于后端未对接，暂时无法真正登录。可以：

**临时方案：跳过登录直接进入首页**

编辑 `pages/login/login.vue`，在 `onLoad` 方法最后添加：
```javascript
onLoad() {
  // 临时：直接模拟登录成功
  setTimeout(() => {
    this.$store.commit('SET_TOKEN', 'mock-token-123456')
    this.$store.commit('SET_USER_INFO', {
      id: '1',
      realName: '测试用户',
      phone: '13800138000',
      deptId: '1',
      deptName: '技术部',
      roleCode: 'USER',  // USER=员工 或 WAREHOUSE=仓管
      roleName: '员工',
      avatar: ''
    })
    uni.reLaunch({ url: '/pages/index/index' })
  }, 500)
}
```

### 切换角色测试

**测试员工角色：**
```javascript
roleCode: 'USER',
roleName: '员工',
```

**测试仓管角色：**
```javascript
roleCode: 'WAREHOUSE',
roleName: '仓库管理员',
```

修改后保存，小程序会自动刷新。

---

## 查看效果

### 员工角色（USER）

**TabBar（4个）：**
- 🏠 首页
- 📝 申请
- 📦 库存
- 👤 我的

**可访问页面：**
- 首页（申请统计、快捷操作）
- 新建申请
- 我的申请（列表、详情）
- 库存查询
- 个人中心
- 消息通知
- 设置

### 仓管角色（WAREHOUSE）

**TabBar（5个）：**
- 🏠 首页
- ✅ 审批
- 📦 库存
- ⚡ 快捷
- 👤 我的

**可访问页面：**
- 首页（今日数据、待办任务）
- 审批管理（列表、详情）
- 库存查询
- 快速入库
- 快速出库
- 待领取出库
- 入库记录
- 出库记录
- 个人中心

---

## Mock 数据（临时测试用）

由于后端未对接，可以在 `utils/request.js` 中添加 mock 数据进行测试。

### 添加 Mock 拦截

在 `$uRequest` 函数最开始添加：

```javascript
export function $uRequest(options = {}) {
  // ========== Mock 数据（临时测试用）==========
  // 登录
  if (options.url === '/api/auth/wechat-login') {
    return Promise.resolve({
      code: 200,
      data: {
        token: 'mock-token-123456',
        userInfo: {
          id: '1',
          realName: '张三',
          phone: '13800138000',
          deptId: '1',
          deptName: '技术部',
          roleCode: 'USER',
          roleName: '员工',
          avatar: ''
        }
      }
    })
  }

  // 申请列表
  if (options.url === '/api/applies/my') {
    return Promise.resolve({
      code: 200,
      data: {
        records: [
          {
            id: '1',
            applyNo: 'AP202501110001',
            status: 0,
            purpose: '办公用品申请',
            materialCount: 3,
            applicantName: '张三',
            applyTime: '2025-01-11 10:00:00',
            isTimeout: false
          }
        ],
        total: 1,
        statusCount: { pending: 1, approved: 0, rejected: 0 }
      }
    })
  }

  // 库存列表
  if (options.url === '/api/inventory') {
    return Promise.resolve({
      code: 200,
      data: {
        records: [
          {
            id: '1',
            materialCode: 'M001',
            materialName: 'A4打印纸',
            spec: '500张/包',
            unit: '包',
            categoryName: '办公用品',
            quantity: 100,
            lockedQuantity: 10,
            availableQuantity: 90,
            minStock: 20,
            stockStatus: 0,
            warehouseName: '技术部仓库',
            location: 'A-01-01'
          }
        ],
        total: 1
      }
    })
  }

  // 其他接口继续请求真实后端...
  // ========== Mock 数据结束 ==========

  return new Promise((resolve, reject) => {
    // ... 原有代码
  })
}
```

---

## 真机预览

### 方法 A：扫码预览

1. 微信开发者工具 → 工具栏 → **预览**
2. 生成二维码
3. 使用微信扫码
4. 在手机上预览

### 方法 B：真机调试

1. 微信开发者工具 → 工具栏 → **真机调试**
2. 扫码连接手机
3. 可在电脑端调试手机上的小程序

---

## 常见问题

### 1. HBuilderX 找不到微信开发者工具

**解决：**
1. 工具 → 设置 → 运行配置
2. 小程序运行配置
3. 微信开发者工具路径：选择微信开发者工具的安装路径

### 2. 编译失败

**检查：**
- ✅ Node.js 是否安装（需要 >= 12.0.0）
- ✅ 依赖是否安装（`npm install`）
- ✅ 路径中是否有中文或特殊字符

### 3. 页面空白

**检查：**
- ✅ 控制台是否有报错
- ✅ 是否已配置 AppID 或使用测试号
- ✅ pages.json 配置是否正确

### 4. 网络请求失败

**检查：**
- ✅ 是否取消了"校验合法域名"（开发时必须）
- ✅ 后端服务是否启动
- ✅ API 地址是否正确

---

## 调试技巧

### 1. 查看日志

**控制台：**
```javascript
console.log('数据：', data)
console.table(list)  // 表格形式展示
```

**真机调试：**
1. 开发者工具 → 调试 → 远程调试
2. 选择连接的手机
3. 查看真机日志

### 2. 查看网络请求

1. 调试器 → Network
2. 查看所有请求和响应
3. 检查请求参数和返回数据

### 3. 查看 Vuex 状态

```javascript
// 在任意页面打印
console.log('Vuex State:', this.$store.state)
console.log('用户信息:', this.$store.state.userInfo)
```

### 4. 断点调试

1. 在代码中添加 `debugger`
2. 代码执行到此处会暂停
3. 可查看变量值、调用栈等

---

## 下一步

1. ✅ **预览效果** - 查看所有页面
2. ✅ **测试功能** - 尝试各种操作
3. ✅ **切换角色** - 体验员工/仓管视图
4. ⏳ **对接后端** - 连接真实 API
5. ⏳ **完整测试** - 测试完整业务流程

---

## 快速命令

```bash
# 安装依赖
npm install

# 运行开发环境
npm run dev:mp-weixin

# 构建生产版本
npm run build:mp-weixin

# 查看项目信息
cat README.md
cat QUICKSTART.md
```

---

**祝调试顺利！** 🎉

如有问题，请查看：
- `README.md` - 项目说明
- `QUICKSTART.md` - 快速启动指南
- `DEPLOYMENT.md` - 部署指南
