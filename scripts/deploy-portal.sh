#!/bin/bash
# 前端门户网站部署
# 用法: ./scripts/deploy-portal.sh
set -e

SERVER_HOST="请改成你的服务器IP或域名"
SERVER_USER="root"
SERVER_PORTAL_DIR="/data/dulizhan/portal"
NODE_PORT="3000"

echo "🔨 构建前端..."

# 1. 本地构建
(cd portal && npm run build)

# 2. standalone 模式需要手动补全 public 和 static
echo "  → 补全 standalone 目录..."
cp -r portal/public portal/.next/standalone/public
cp -r portal/.next/static portal/.next/standalone/.next/static

# 3. 打包
echo "  → 打包 standalone 产物..."
cd portal/.next/standalone
tar -czf /tmp/portal-standalone.tar.gz .
cd - > /dev/null

echo ""
echo "📦 上传到服务器 ${SERVER_USER}@${SERVER_HOST}..."

# 4. 上传
ssh ${SERVER_USER}@${SERVER_HOST} "mkdir -p ${SERVER_PORTAL_DIR}"
scp /tmp/portal-standalone.tar.gz ${SERVER_USER}@${SERVER_HOST}:${SERVER_PORTAL_DIR}/

# 5. 解压
ssh ${SERVER_USER}@${SERVER_HOST} "cd ${SERVER_PORTAL_DIR} && tar -xzf portal-standalone.tar.gz && rm portal-standalone.tar.gz"

echo ""
echo "🚀 用 PM2 启动/重启..."

# 6. PM2 启动或重启
ssh ${SERVER_USER}@${SERVER_HOST} "
  if pm2 describe dulizhan-portal > /dev/null 2>&1; then
    pm2 restart dulizhan-portal
  else
    cd ${SERVER_PORTAL_DIR} && pm2 start server.js --name dulizhan-portal
  fi
  pm2 save
"

echo ""
echo "✅ 前端部署完成！Node 运行在 ${SERVER_HOST}:${NODE_PORT}"
echo ""
echo "⚠️  接下来在 1Panel 中配置 OpenResty:"
echo "  网站 → 创建网站 → 反向代理"
echo "  域名: 你的前端域名"
echo "  代理地址: http://127.0.0.1:${NODE_PORT}"
