#!/bin/sh
# 用环境变量 ADMIN_SECRET 替换模板中的占位符，然后启动 nginx
set -e

export ADMIN_SECRET="${ADMIN_SECRET:-change-me-to-a-random-secret}"

# 替换 nginx 配置模板中的变量
envsubst '${ADMIN_SECRET}' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf

# 启动 nginx（前台运行）
exec nginx -g 'daemon off;'
