#!/bin/bash
# 生成管理后台的签名访问链接
# 用法: ./scripts/gen-admin-link.sh [有效期，默认24h]

set -e

SECRET="${ADMIN_SECRET:-请设置ADMIN_SECRET环境变量}"
HOURS="${1:-24}"

EXPIRES=$(date -d "+${HOURS} hours" +%s)

# 签名公式: md5(expires + "/admin" + secret)
HASH=$(echo -n "${EXPIRES}/admin ${SECRET}" | openssl md5 -binary | openssl base64 | tr +/ -_ | tr -d =)

echo ""
echo " 管理后台签名链接（${HOURS}小时内有效）"
echo "============================================="
echo " http://43.156.124.28/admin/?md5=${HASH}&expires=${EXPIRES}"
echo "============================================="
echo " 过期时间: $(date -d @${EXPIRES} '+%Y-%m-%d %H:%M:%S')"
echo ""
echo " 首次访问后浏览器会记住 7 天，期间无需重新签名。"
