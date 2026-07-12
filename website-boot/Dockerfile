# ---- 构建阶段 ----
ARG NODE_IMAGE=node:20-alpine
ARG NGINX_IMAGE=nginx:alpine
FROM ${NODE_IMAGE} AS build
WORKDIR /app
RUN corepack enable && corepack prepare pnpm@9 --activate
COPY package.json pnpm-lock.yaml ./
ARG NPM_REGISTRY=https://registry.npmjs.org/
RUN pnpm config set registry ${NPM_REGISTRY} && pnpm install --frozen-lockfile
COPY . .
# pnpm-workspace.yaml 缺少 packages 字段，补上
RUN printf 'packages:\n  - .\n\n' | cat - pnpm-workspace.yaml > temp && mv temp pnpm-workspace.yaml
ARG VITE_API_URL=
ARG VITE_BASE_URL=/osengroup/
ENV VITE_API_URL=${VITE_API_URL}
ENV VITE_BASE_URL=${VITE_BASE_URL}
RUN pnpm build

# ---- 运行阶段 ----
FROM ${NGINX_IMAGE}
COPY --from=build /app/dist /usr/share/nginx/html
# SPA 路由回退
RUN echo 'server { \
  listen 80; \
  root /usr/share/nginx/html; \
  index index.html; \
  location / { \
    try_files $uri $uri/ /index.html; \
  } \
}' > /etc/nginx/conf.d/default.conf
EXPOSE 80
