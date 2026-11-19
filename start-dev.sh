#!/usr/bin/env bash
# =============================================================
# CT-Tibet-WMS 一键启动（Mac / Linux）
#
# 依次完成：
#   1. 首次运行时生成 .env.local（JWT 密钥、数据库账号），之后一直复用
#   2. 检查 MySQL；数据库还没建时自动执行三个初始化脚本
#   3. 检查 Redis；没在运行且本机装了 redis-server 时自动启动
#   4. 编译并在后台启动后端（端口 48888）
#   5. 在后台启动 PC 前端（端口 4444）
#
# 用法：
#   ./start-dev.sh               启动全部
#   ./start-dev.sh --no-build    后端不重新编译（代码没改时更快）
#   ./start-dev.sh --backend-only  只启动后端
# 停止：./stop-dev.sh
# 日志：.run/backend.log、.run/frontend.log
# =============================================================
set -uo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

RUN_DIR="$ROOT/.run"
ENV_FILE="$ROOT/.env.local"
BACKEND_PORT=48888
FRONTEND_PORT=4444
DB_NAME=ct_tibet_wms
JAR="$ROOT/backend/target/ct-tibet-wms.jar"

BUILD=1
START_FRONTEND=1
for arg in "$@"; do
  case "$arg" in
    --no-build) BUILD=0 ;;
    --backend-only) START_FRONTEND=0 ;;
    -h|--help) sed -n '2,20p' "$0"; exit 0 ;;
    *) echo "未知参数：$arg（可用 --no-build、--backend-only）"; exit 1 ;;
  esac
done

step() { printf '\n\033[1;34m▶ %s\033[0m\n' "$*"; }
ok()   { printf '  \033[32m✔ %s\033[0m\n' "$*"; }
warn() { printf '  \033[33m! %s\033[0m\n' "$*"; }
fail() { printf '  \033[31m✘ %s\033[0m\n' "$*"; exit 1; }

# 同时检查 IPv4 和 IPv6（新版 Node 在 Mac 上可能让 Vite 只监听 ::1）
port_open() { (exec 3<>"/dev/tcp/127.0.0.1/$1") 2>/dev/null || (exec 3<>"/dev/tcp/::1/$1") 2>/dev/null; }

# 等待端口打开；进程提前退出或超时则返回失败
wait_port() {
  local port=$1 pid=$2 seconds=$3
  for ((i = 0; i < seconds; i++)); do
    port_open "$port" && return 0
    kill -0 "$pid" 2>/dev/null || return 1
    sleep 1
  done
  return 1
}

mkdir -p "$RUN_DIR"

# ---------- 1. 配置 ----------
step "读取配置 .env.local"
if [ ! -f "$ENV_FILE" ]; then
  if command -v openssl >/dev/null 2>&1; then
    SECRET="$(openssl rand -hex 32)"
  else
    SECRET="$(head -c 32 /dev/urandom | od -An -tx1 | tr -d ' \n')"
  fi
  cat > "$ENV_FILE" <<EOF
# 本地开发配置（由 start-dev 自动生成，已被 .gitignore 忽略，不会提交到 Git）
# 登录令牌签名密钥：至少 32 个字符。改了之后所有人需要重新登录。
JWT_SECRET=$SECRET
# MySQL 账号密码：和你本机 MySQL 不一致时改这里
DB_USER=root
DB_PASSWORD=root
EOF
  ok "首次运行，已生成 .env.local（随机 JWT 密钥）"
else
  ok "使用已有的 .env.local"
fi
set -a
# shellcheck disable=SC1090
. "$ENV_FILE"
set +a
[ "${#JWT_SECRET}" -ge 32 ] || fail ".env.local 中 JWT_SECRET 少于 32 个字符，请修改或删除该文件后重新运行"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-root}"
# 让后端使用 .env.local 里的数据库账号（覆盖 application-dev.yml 中的默认值）
export SPRING_DATASOURCE_USERNAME="$DB_USER" SPRING_DATASOURCE_PASSWORD="$DB_PASSWORD"

# ---------- 2. 检查软件 ----------
step "检查必需软件"
for cmd in java mvn; do
  command -v "$cmd" >/dev/null 2>&1 || fail "没有找到 $cmd，请先安装（见 README『安装软件』）"
done
if [ "$START_FRONTEND" = 1 ]; then
  for cmd in node npm; do
    command -v "$cmd" >/dev/null 2>&1 || fail "没有找到 $cmd，请先安装 Node.js 18+"
  done
fi
ok "Java、Maven$([ "$START_FRONTEND" = 1 ] && echo '、Node.js') 已安装"

# ---------- 3. MySQL ----------
step "检查 MySQL"
port_open 3306 || fail "MySQL 没有运行（3306 端口无响应）。请先启动 MySQL 服务"
ok "MySQL 正在运行"
if command -v mysql >/dev/null 2>&1; then
  export MYSQL_PWD="$DB_PASSWORD"
  TABLES="$(mysql -u"$DB_USER" -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB_NAME'" 2>&1)" \
    || fail "无法用账号 $DB_USER 登录 MySQL：$TABLES
     请在 .env.local 中改成正确的 DB_USER / DB_PASSWORD"
  if [ "$TABLES" = "0" ]; then
    warn "数据库 $DB_NAME 还没有初始化，开始建库并导入示例数据..."
    for f in create_database.sql schema.sql init_data.sql; do
      db=""; [ "$f" != create_database.sql ] && db="$DB_NAME"
      mysql -u"$DB_USER" --default-character-set=utf8mb4 $db < "sql/$f" > "$RUN_DIR/db-init.log" 2>&1 \
        || fail "执行 sql/$f 失败，详情见 .run/db-init.log"
    done
    ok "数据库初始化完成（默认账号密码均为 123456）"
  else
    ok "数据库 $DB_NAME 已存在（$TABLES 张表）"
  fi
  unset MYSQL_PWD
else
  warn "没有找到 mysql 命令行工具，跳过数据库检查；如果还没建库，请按 README「手动 1. 初始化数据库」执行"
fi

# ---------- 4. Redis ----------
step "检查 Redis"
if port_open 6379; then
  ok "Redis 正在运行"
elif command -v redis-server >/dev/null 2>&1; then
  redis-server --daemonize yes --logfile "$RUN_DIR/redis.log" > /dev/null 2>&1 < /dev/null
  for _ in 1 2 3 4 5; do port_open 6379 && break; sleep 1; done
  port_open 6379 || fail "Redis 启动失败，详情见 .run/redis.log"
  ok "已在后台启动 Redis"
else
  fail "Redis 没有运行，也没有找到 redis-server。请先安装并启动 Redis"
fi

# ---------- 5. 后端 ----------
step "启动后端（端口 $BACKEND_PORT）"
if port_open "$BACKEND_PORT"; then
  warn "端口 $BACKEND_PORT 已被占用，后端可能已经在运行，跳过。需要重启请先执行 ./stop-dev.sh"
else
  if [ "$BUILD" = 1 ] || [ ! -f "$JAR" ]; then
    echo "  编译中（第一次需要下载依赖，可能要几分钟）..."
    (cd backend && mvn -q -B -DskipTests package) > "$RUN_DIR/build.log" 2>&1 \
      || { tail -n 30 "$RUN_DIR/build.log"; fail "后端编译失败，完整日志见 .run/build.log"; }
    ok "编译完成"
  fi
  nohup java -Dfile.encoding=UTF-8 -jar "$JAR" > "$RUN_DIR/backend.log" 2>&1 < /dev/null &
  BACKEND_PID=$!
  echo "$BACKEND_PID" > "$RUN_DIR/backend.pid"
  echo "  等待后端启动（通常 20~60 秒）..."
  if wait_port "$BACKEND_PORT" "$BACKEND_PID" 180; then
    ok "后端已启动"
  else
    echo "  ---- 后端日志最后 30 行 ----"
    tail -n 30 "$RUN_DIR/backend.log"
    fail "后端启动失败，完整日志见 .run/backend.log（常见原因见 README『常见启动问题』）"
  fi
fi

# ---------- 6. PC 前端 ----------
if [ "$START_FRONTEND" = 1 ]; then
  step "启动 PC 前端（端口 $FRONTEND_PORT）"
  if port_open "$FRONTEND_PORT"; then
    warn "端口 $FRONTEND_PORT 已被占用，前端可能已经在运行，跳过"
  else
    if [ ! -d frontend-pc/node_modules ]; then
      echo "  首次运行，安装前端依赖（可能要几分钟）..."
      (cd frontend-pc && npm install) > "$RUN_DIR/npm-install.log" 2>&1 \
        || { tail -n 30 "$RUN_DIR/npm-install.log"; fail "npm install 失败，完整日志见 .run/npm-install.log"; }
    fi
    # 重定向放在整个后台子进程上，避免它占住当前终端的输出
    (cd frontend-pc && exec npm run dev) > "$RUN_DIR/frontend.log" 2>&1 < /dev/null &
    FRONTEND_PID=$!
    echo "$FRONTEND_PID" > "$RUN_DIR/frontend.pid"
    if wait_port "$FRONTEND_PORT" "$FRONTEND_PID" 60; then
      ok "前端已启动"
    else
      tail -n 30 "$RUN_DIR/frontend.log"
      fail "前端启动失败，完整日志见 .run/frontend.log"
    fi
  fi
fi

# ---------- 完成 ----------
cat <<EOF

$(printf '\033[1;32m')全部启动完成$(printf '\033[0m')
  PC 端：      http://localhost:$FRONTEND_PORT/wms/
  API 文档：   http://localhost:$BACKEND_PORT/doc.html
  测试账号：   admin / wl_warehouse / wl_user1，密码均为 123456
  小程序：     用 HBuilderX 运行 miniprogram 目录（后端地址 http://localhost:$BACKEND_PORT）
  查看日志：   tail -f .run/backend.log
  停止服务：   ./stop-dev.sh
EOF
