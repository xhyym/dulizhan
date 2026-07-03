#!/bin/bash
# 本地构建前端并打包
# 用法: ./scripts/build-portal.sh
set -e

echo "🔨 构建前端（API_BASE=空，走相对路径，由 OpenResty 统一代理）..."
(cd portal && NEXT_PUBLIC_API_URL="" npm run build)

echo "→ 补全 standalone 目录..."
cp -r portal/public portal/.next/standalone/public
cp -r portal/.next/static portal/.next/standalone/.next/static

echo "→ 打包..."
rm -f portal/portal-standalone.tar.gz
tar -czf portal/portal-standalone.tar.gz -C portal/.next/standalone .

echo ""
echo "============================================"
echo "✅ 打包完成: portal/portal-standalone.tar.gz"
echo "============================================"
echo ""
echo "📋 服务器部署步骤:"
echo ""
echo "  1. 上传压缩包:"
echo "     scp portal/portal-standalone.tar.gz root@43.156.124.28:/data/dulizhan/portal/"
echo ""
echo "  2. SSH 到服务器解压:"
echo "     ssh root@43.156.124.28"
echo "     cd /data/dulizhan/portal"
echo "     tar -xzf portal-standalone.tar.gz"
echo ""
echo "  3. PM2 启动:"
echo "     pm2 start server.js --name dulizhan-portal"
echo "     pm2 save"
echo ""
echo "  4. 1Panel 创建网站 → 反向代理，域名: 43.156.124.28"
echo "     ┌──────────┬──────────────────────────┐"
echo "     │ 路径     │ 代理地址                  │"
echo "     ├──────────┼──────────────────────────┤"
echo "     │ /api     │ http://127.0.0.1:8080     │"
echo "     │ /        │ http://127.0.0.1:3000     │"
echo "     └──────────┴──────────────────────────┘"
echo ""
echo "  注意: /api 规则必须排在 / 前面！"
