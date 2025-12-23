# 前端数据渲染问题排查指南

**问题**: 物资管理页面报错 `TypeError: data2 is not iterable`
**状态**: 🔍 排查中

---

## 错误信息

```
Uncaught (in promise) TypeError: data2 is not iterable
    at checkSelectedStatus (watcher.ts:214:25)
    at Object.updateAllSelected (watcher.ts:230:28)
    at Object.setData (index.ts:48:22)
```

**分析**: 这是Element Plus表格组件的错误，说明传给`el-table`的`:data`属性不是数组。

---

## 立即排查步骤

### 步骤1: 刷新浏览器并查看控制台

1. **打开浏览器开发者工具**
   - 按F12或右键 → 检查
   - 切换到"Console"标签

2. **刷新页面**
   - 按Ctrl+R或F5

3. **查看调试日志**

应该看到以下日志：
```
API响应: { code: 200, message: "成功", data: [...], total: 20 }
数据类型: object false
res.data类型: object true
```

**如果看到上面的日志**：说明API正常，继续下一步
**如果没有看到日志**：说明请求失败，跳到"API请求失败"章节

---

### 步骤2: 检查控制台输出

#### 正常情况
```javascript
API响应: {
  code: 200,
  message: "查询成功",
  data: Array(20),  // ✓ 这里应该是Array
  total: 20,
  pageNum: 1,
  pageSize: 20
}
数据类型: object false
res.data类型: object true  // ✓ 这里应该是true
```

#### 异常情况1: data不是数组
```javascript
API响应: {
  code: 200,
  data: null,  // ✗ data是null
  total: 0
}
```
**解决**: 后端返回了null，需要检查后端Service层

#### 异常情况2: 整个响应是数组
```javascript
API响应: Array(20)  // ✗ 整个响应是数组
数据类型: object true
```
**解决**: axios拦截器可能有问题，需要检查request.js

#### 异常情况3: 401未授权
```javascript
响应错误: Error: 未授权
```
**解决**: Token过期或无效，需要重新登录

---

### 步骤3: 检查网络请求

1. **切换到Network标签**
2. **找到materials请求**
3. **查看响应数据**

#### 检查请求
```
Request URL: http://localhost:48888/api/materials?pageNum=1&pageSize=20
Request Method: GET
Status Code: 200 OK
```

#### 检查响应头
```
Authorization: Bearer eyJhbGci...  (应该有Token)
```

#### 检查响应体
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "materialCode": "GX001",
      "materialName": "光缆12芯",
      ...
    }
  ],
  "total": 20,
  "pageNum": 1,
  "pageSize": 20
}
```

---

## 常见问题和解决方案

### 问题1: 401未授权

**症状**:
- 控制台显示"未授权"或"登录过期"
- Network显示401状态码

**解决**:
```bash
# 方案1: 重新登录
访问 http://localhost:5173/login
使用 admin / 123456 登录

# 方案2: 清除缓存
localStorage.clear()
然后刷新页面重新登录
```

---

### 问题2: 后端服务未启动

**症状**:
- 控制台显示"无法连接到服务器"
- Network显示请求失败（红色）

**解决**:
```bash
# 检查后端是否运行
curl http://localhost:48888/actuator/health

# 启动后端服务
cd backend
mvn spring-boot:run
```

---

### 问题3: 数据库连接失败

**症状**:
- 后端返回500错误
- 后端日志显示数据库连接错误

**解决**:
```bash
# 检查MySQL是否运行
mysql -u root -proot -e "SELECT 1"

# 检查数据库是否存在
mysql -u root -proot -e "USE ct_tibet_wms; SELECT COUNT(*) FROM tb_material;"
```

---

### 问题4: CORS跨域错误

**症状**:
- 控制台显示CORS错误
- Network显示请求被阻止

**解决**:
检查后端CORS配置（应该已经配置好）：
```java
// backend/src/main/java/com/ct/wms/config/SecurityConfig.java
configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
```

---

### 问题5: Element Plus表格数据格式错误

**症状**:
- `TypeError: data2 is not iterable`
- 表格无法渲染

**原因分析**:
1. `tableData.value` 不是数组
2. 数据在更新过程中变成了其他类型

**解决**:
已在代码中添加了严格的类型检查：
```javascript
if (res && Array.isArray(res.data)) {
  tableData.value = res.data
} else {
  tableData.value = []  // 确保始终是数组
}
```

---

## 手动测试API

### 使用curl测试

```bash
# 1. 登录获取Token
curl -X POST http://localhost:48888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 保存返回的token

# 2. 测试物资列表API
curl -X GET "http://localhost:48888/api/materials?pageNum=1&pageSize=20" \
  -H "Authorization: Bearer <你的token>" \
  | python -m json.tool
```

### 期望响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "materialCode": "GX001",
      "materialName": "光缆12芯",
      "category": "光缆类",
      "spec": "12芯单模",
      "unit": "条",
      "price": 1500.00,
      "minStock": 100.00,
      "status": 0
    }
  ],
  "total": 20,
  "pageNum": 1,
  "pageSize": 20,
  "pages": 1
}
```

---

## 代码修复历史

### 修复1: 数据格式适配
```javascript
// 错误代码
tableData.value = res.data.list  // ✗ data下没有list

// 正确代码
tableData.value = res.data       // ✓ data就是数组
```

### 修复2: 容错处理
```javascript
// 添加类型检查和容错
if (res && Array.isArray(res.data)) {
  tableData.value = res.data
} else {
  console.warn('响应数据格式异常:', res)
  tableData.value = []
  pagination.total = 0
}
```

### 修复3: 异常处理
```javascript
catch (error) {
  console.error('查询物资列表失败:', error)
  ElMessage.error('查询物资列表失败')
  // 确保出错时也是空数组
  tableData.value = []
  pagination.total = 0
}
```

---

## 下一步调试方案

### 方案A: 查看详细日志

1. 刷新浏览器
2. 打开控制台
3. 查看以下日志输出：
   - `API响应:`
   - `数据类型:`
   - `res.data类型:`

4. 将日志信息提供给我，我会进一步分析

### 方案B: 检查后端日志

```bash
# 查看后端日志
tail -f backend/logs/spring.log

# 或者在IDE中查看控制台输出
```

查找以下关键信息：
- SQL查询是否成功
- 返回的数据条数
- 是否有异常堆栈

### 方案C: 简化测试

创建一个最简单的测试页面：
```vue
<template>
  <div>
    <el-button @click="test">测试API</el-button>
    <pre>{{ result }}</pre>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { listMaterials } from '@/api/material'

const result = ref(null)

const test = async () => {
  try {
    const res = await listMaterials({ pageNum: 1, pageSize: 10 })
    result.value = JSON.stringify(res, null, 2)
    console.log('测试结果:', res)
  } catch (error) {
    result.value = 'Error: ' + error.message
    console.error('测试失败:', error)
  }
}
</script>
```

---

## 联系支持

如果上述方法都无法解决问题，请提供以下信息：

1. **浏览器控制台完整日志** (包括所有Console输出)
2. **Network标签的materials请求详情** (请求和响应)
3. **后端日志** (最近50行)
4. **错误截图**

---

**文档版本**: v1.0
**最后更新**: 2025-12-12
