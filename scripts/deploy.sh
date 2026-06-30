#!/bin/bash
# 本地编译 + 部署到服务器
# 用法: ./scripts/deploy.sh
set -e

# ===== 配置 =====
SERVER_HOST="请改成你的服务器IP或域名"
SERVER_USER="root"
SERVER_DIR="/opt/dulizhan"

echo "🔨 开始本地编译..."

# 1. 编译后端 JAR
echo "  → 编译后端..."
mvn package -DskipTests -q
echo "  ✓ JAR 编译完成: target/admin-boot-1.0.0-SNAPSHOT.jar"

# 2. 编译前端（如果需要）
# echo "  → 编译前端..."
# (cd portal && npm run build)
# echo "  ✓ 前端编译完成"

echo ""
echo "📦 上传到服务器 ${SERVER_USER}@${SERVER_HOST}..."

# 3. 上传 JAR
ssh ${SERVER_USER}@${SERVER_HOST} "mkdir -p ${SERVER_DIR}/target"
scp target/admin-boot-1.0.0-SNAPSHOT.jar ${SERVER_USER}@${SERVER_HOST}:${SERVER_DIR}/target/

# 4. 上传 Docker 配置
scp Dockerfile docker-compose.yml ${SERVER_USER}@${SERVER_HOST}:${SERVER_DIR}/

# 上传 .env（如果存在）
if [ -f .env ]; then
    scp .env ${SERVER_USER}@${SERVER_HOST}:${SERVER_DIR}/
fi

# 5. 上传前端构建产物（如果需要）
# ssh ${SERVER_USER}@${SERVER_HOST} "mkdir -p ${SERVER_DIR}/portal/.next/standalone"
# scp -r portal/.next/standalone/* ${SERVER_USER}@${SERVER_HOST}:${SERVER_DIR}/portal/.next/standalone/

echo ""
echo "🚀 服务器重启容器..."

# 6. 在服务器上重新构建并启动
ssh ${SERVER_USER}@${SERVER_HOST} "cd ${SERVER_DIR} && docker compose up -d --build"

echo ""
echo "✅ 部署完成!"
echo "  后端: http://${SERVER_HOST}:8080"
