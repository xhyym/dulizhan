# ================= 构建阶段：编译 website-boot（管理后台）=================
# 构建上下文为仓库根目录（见 docker-compose.yml 的 gateway.build.context: .）
ARG NODE_IMAGE=node:20-alpine
ARG NGINX_IMAGE=nginx:alpine

FROM ${NODE_IMAGE} AS admin-build
WORKDIR /app
RUN corepack enable && corepack prepare pnpm@9 --activate

ARG NPM_REGISTRY=https://registry.npmjs.org/
COPY website-boot/package.json website-boot/pnpm-lock.yaml ./
RUN pnpm config set registry ${NPM_REGISTRY} && pnpm install --frozen-lockfile

COPY website-boot/ .
# pnpm-workspace.yaml 缺少 packages 字段，补上（沿用项目原有处理）
RUN printf 'packages:\n  - .\n\n' | cat - pnpm-workspace.yaml > temp && mv temp pnpm-workspace.yaml

# 管理后台部署在 /osengroup/ 子路径；API 用相对路径（同源经网关转发到 /api）
ARG VITE_BASE_URL=/osengroup/
ARG VITE_API_URL=
ENV VITE_BASE_URL=${VITE_BASE_URL}
ENV VITE_API_URL=${VITE_API_URL}
RUN pnpm build

# ================= 运行阶段：nginx 网关 =================
FROM ${NGINX_IMAGE}

# 管理后台静态产物
COPY --from=admin-build /app/dist /usr/share/nginx/html/osengroup

# nginx 配置模板（启动时 envsubst 注入 ADMIN_SECRET）
COPY gateway/nginx.conf.template /etc/nginx/conf.d/default.conf.template
COPY gateway/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/entrypoint.sh"]
