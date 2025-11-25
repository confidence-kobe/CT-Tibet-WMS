# 启动脚本使用指南

本项目提供了多个便捷脚本,帮助快速启动和停止开发/生产环境。

---

## 📋 脚本列表

### 开发环境脚本

| 脚本名称 | 平台 | 用途 | 说明 |
|---------|------|------|------|
| `start-dev.sh` | Linux/Mac | 启动开发环境 | 同时启动后端和PC前端 |
| `start-dev.bat` | Windows | 启动开发环境 | 同时启动后端和PC前端 |
| `stop-dev.sh` | Linux/Mac | 停止开发环境 | 停止所有开发服务 |

### Docker环境脚本

| 脚本名称 | 平台 | 用途 | 说明 |
|---------|------|------|------|
| `docker-start.sh` | Linux/Mac | 启动Docker环境 | 支持开发/生产环境选择 |
| `docker-stop.sh` | Linux/Mac | 停止Docker环境 | 停止所有Docker容器 |

### 测试脚本

| 脚本名称 | 平台 | 用途 | 说明 |
|---------|------|------|------|
| `backend/run-tests.sh` | Linux/Mac | 运行后端测试 | 执行所有单元和集成测试 |
| `backend/run-tests.bat` | Windows | 运行后端测试 | 执行所有单元和集成测试 |

---

## 🚀 快速开始

### 方式一: 使用开发脚本 (推荐用于开发)

#### Linux/Mac

```bash
# 1. 赋予脚本可执行权限(首次运行)
chmod +x start-dev.sh stop-dev.sh

# 2. 启动开发环境
./start-dev.sh

# 3. 访问系统
# 前端: http://localhost:5173
# 后端: http://localhost:8888

# 4. 停止开发环境
./stop-dev.sh
```

#### Windows

```cmd
# 1. 启动开发环境
start-dev.bat

# 2. 访问系统
# 前端: http://localhost:5173
# 后端: http://localhost:8888

# 3. 停止: 关闭打开的命令窗口
```

### 方式二: 使用Docker (推荐用于生产)

#### Linux/Mac

```bash
# 1. 赋予脚本可执行权限(首次运行)
chmod +x docker-start.sh docker-stop.sh

# 2. 启动Docker环境
./docker-start.sh

# 选择环境:
# 1) 开发环境 (development)
# 2) 生产环境 (production)

# 3. 访问系统
# 开发环境: http://localhost:8080
# 生产环境: http://localhost:80

# 4. 停止Docker环境
./docker-stop.sh
```

---

## 📖 详细说明

### start-dev.sh / start-dev.bat

**功能**: 一键启动后端和前端开发服务器

**前置要求**:
- JDK 11+
- Maven 3.8+
- Node.js 16+
- npm

**执行流程**:
1. 检查环境(Java, Maven, Node.js, npm)
2. 构建并启动后端服务(Maven)
3. 安装依赖(如需要)
4. 启动PC前端服务(Vite)

**启动的服务**:
- 后端API: `http://localhost:8888`
- PC前端: `http://localhost:5173`
- API文档: `http://localhost:8888/swagger-ui.html`

**默认账号**:
- 用户名: `admin`
- 密码: `123456`

**停止服务**:
- Linux/Mac: 按 `Ctrl+C` 或运行 `./stop-dev.sh`
- Windows: 关闭命令窗口

---

### stop-dev.sh

**功能**: 停止所有开发服务

**停止方式**:
1. 尝试从PID文件读取进程ID并停止
2. 尝试通过端口(8888, 5173)查找进程并停止
3. 停止所有Maven和Vite进程

**使用场景**:
- 开发完成后清理进程
- start-dev.sh异常退出后清理残留进程
- 释放端口供其他应用使用

---

### docker-start.sh

**功能**: 使用Docker Compose启动整个系统

**前置要求**:
- Docker 20.10+
- Docker Compose v2.0+ 或 docker-compose 1.29+

**环境选择**:

**1. 开发环境 (development)**
- 使用: `docker-compose.yml`
- 特点:
  - 代码热重载
  - 详细日志输出
  - 开发工具端口暴露
- 访问: `http://localhost:8080`

**2. 生产环境 (production)**
- 使用: `docker-compose.prod.yml`
- 特点:
  - 优化的镜像大小
  - 生产级配置
  - 安全加固
- 访问: `http://localhost:80`

**包含的服务**:
- MySQL 8.0 (数据库)
- Redis 7.x (缓存,可选)
- Backend (Spring Boot后端)
- Frontend (Nginx + Vue前端)

**常用命令**:
```bash
# 查看日志
docker-compose logs -f

# 查看服务状态
docker-compose ps

# 进入容器
docker-compose exec backend bash

# 重启服务
docker-compose restart backend
```

---

### docker-stop.sh

**功能**: 停止所有Docker容器

**停止范围**:
- 开发环境容器
- 生产环境容器
- 相关网络和卷(根据配置)

**可选清理**:
- 提示是否清理未使用的Docker镜像
- 释放磁盘空间

---

### run-tests.sh / run-tests.bat

**功能**: 运行后端所有测试

**位置**: `backend/`目录下

**执行的测试**:
- 单元测试 (45个)
- 集成测试 (5个)

**输出**:
- 测试通过/失败统计
- 代码覆盖率报告
- JaCoCo HTML报告

**使用方法**:

Linux/Mac:
```bash
cd backend
chmod +x run-tests.sh
./run-tests.sh
```

Windows:
```cmd
cd backend
run-tests.bat
```

**查看覆盖率报告**:
```
backend/target/site/jacoco/index.html
```

---

## 🔧 故障排查

### 端口被占用

**问题**: 启动时提示端口8888或5173被占用

**解决**:

Linux/Mac:
```bash
# 查找占用进程
lsof -ti:8888
lsof -ti:5173

# 停止进程
kill -9 <PID>

# 或使用stop-dev.sh自动清理
./stop-dev.sh
```

Windows:
```cmd
# 查找占用进程
netstat -ano | findstr :8888
netstat -ano | findstr :5173

# 停止进程
taskkill /PID <PID> /F
```

### Maven构建失败

**问题**: Maven构建或启动失败

**解决**:
1. 检查Java版本: `java -version` (需要11+)
2. 检查Maven版本: `mvn -version` (需要3.8+)
3. 清理Maven缓存:
   ```bash
   cd backend
   mvn clean
   rm -rf ~/.m2/repository  # 清理本地仓库(可选)
   ```
4. 配置Maven镜像(如果网络慢):
   ```xml
   <!-- ~/.m2/settings.xml -->
   <mirrors>
     <mirror>
       <id>aliyun</id>
       <mirrorOf>central</mirrorOf>
       <url>https://maven.aliyun.com/repository/public</url>
     </mirror>
   </mirrors>
   ```

### npm安装失败

**问题**: npm install失败或很慢

**解决**:
1. 检查Node.js版本: `node -v` (需要16+)
2. 使用国内镜像:
   ```bash
   npm config set registry https://registry.npmmirror.com
   ```
3. 清理npm缓存:
   ```bash
   npm cache clean --force
   ```
4. 删除node_modules重新安装:
   ```bash
   cd frontend-pc
   rm -rf node_modules package-lock.json
   npm install
   ```

### Docker启动失败

**问题**: Docker容器启动失败

**解决**:
1. 检查Docker是否运行: `docker ps`
2. 检查端口占用:
   - MySQL: 3306
   - Redis: 6379
   - Backend: 8888
   - Frontend: 8080/80
3. 查看容器日志:
   ```bash
   docker-compose logs backend
   docker-compose logs frontend
   ```
4. 完全清理重启:
   ```bash
   docker-compose down -v  # 删除卷
   docker system prune -a  # 清理系统(谨慎使用)
   ./docker-start.sh
   ```

### 数据库连接失败

**问题**: 后端无法连接数据库

**解决**:
1. 检查MySQL是否运行
2. 检查配置文件 `backend/src/main/resources/application-dev.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/wms?useUnicode=true&characterEncoding=utf8&useSSL=false
       username: root
       password: 123456
   ```
3. 确保数据库已创建: `CREATE DATABASE IF NOT EXISTS wms;`
4. 导入初始数据: 运行 `backend/src/main/resources/db/schema.sql`

---

## 💡 最佳实践

### 开发工作流

1. **启动开发环境**
   ```bash
   ./start-dev.sh
   ```

2. **进行开发**
   - 后端代码改动会自动重新编译(Spring Boot DevTools)
   - 前端代码改动会自动热重载(Vite HMR)

3. **运行测试**
   ```bash
   cd backend
   ./run-tests.sh
   ```

4. **提交代码前**
   - 确保所有测试通过
   - 检查代码风格
   - 更新文档(如需要)

5. **停止开发环境**
   ```bash
   ./stop-dev.sh
   ```

### Docker部署流程

1. **构建镜像**
   ```bash
   docker-compose build
   ```

2. **启动服务**
   ```bash
   ./docker-start.sh
   # 选择生产环境
   ```

3. **健康检查**
   ```bash
   docker-compose ps
   curl http://localhost/api/health
   ```

4. **查看日志**
   ```bash
   docker-compose logs -f backend
   ```

5. **停止服务**
   ```bash
   ./docker-stop.sh
   ```

---

## 📚 相关文档

- **快速开始**: [README.md](README.md)
- **部署手册**: [docs/DEPLOYMENT_MANUAL.md](docs/DEPLOYMENT_MANUAL.md)
- **Docker部署**: [docs/DEPLOYMENT_DOCKER.md](docs/DEPLOYMENT_DOCKER.md)
- **CI/CD配置**: [docs/DEPLOYMENT_CICD.md](docs/DEPLOYMENT_CICD.md)
- **故障排查**: [docs/FAQ.md](docs/FAQ.md)

---

## 🆘 获取帮助

如果遇到问题:

1. 查看 [docs/FAQ.md](docs/FAQ.md)
2. 查看脚本输出的错误信息
3. 查看服务日志
4. 在GitHub提Issue: https://github.com/confidence-kobe/CT-Tibet-WMS/issues

---

**最后更新**: 2025-11-25
**维护者**: 开发团队
