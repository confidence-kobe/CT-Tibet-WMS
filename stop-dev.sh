#!/usr/bin/env bash
# =============================================================
# CT-Tibet-WMS 停止开发服务（Mac / Linux）
# 停止由 ./start-dev.sh 启动的后端和 PC 前端。MySQL、Redis 不会被停止。
# =============================================================
set -uo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
RUN_DIR="$ROOT/.run"

# 结束进程及其所有子进程（npm run dev 会再拉起 node/vite 子进程）
kill_tree() {
  local pid=$1 child
  for child in $(pgrep -P "$pid" 2>/dev/null); do
    kill_tree "$child"
  done
  kill "$pid" 2>/dev/null
}

stop_service() {
  local name=$1 file="$RUN_DIR/$2.pid"
  if [ ! -f "$file" ]; then
    echo "  - $name：没有记录（未通过 start-dev.sh 启动，或已停止）"
    return
  fi
  local pid
  pid="$(cat "$file")"
  if kill -0 "$pid" 2>/dev/null; then
    kill_tree "$pid"
    for _ in 1 2 3 4 5 6 7 8 9 10; do kill -0 "$pid" 2>/dev/null || break; sleep 1; done
    kill -0 "$pid" 2>/dev/null && kill -9 "$pid" 2>/dev/null
    echo "  ✔ $name 已停止"
  else
    echo "  - $name：进程已不存在"
  fi
  rm -f "$file"
}

echo "停止 CT-Tibet-WMS 开发服务..."
stop_service "PC 前端" frontend
stop_service "后端" backend
echo "完成（MySQL、Redis 保持运行）"
