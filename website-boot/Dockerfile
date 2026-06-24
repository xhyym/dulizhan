# ---- 构建阶段 ----
FROM node:20-alpine AS build
WORKDIR /app
RUN corepack enable && corepack prepare pnpm@9 --activate
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN printf 'packages:\n  - .\n\n' | cat - pnpm-workspace.yaml > temp && mv temp pnpm-workspace.yaml
RUN pnpm install --frozen-lockfile
COPY . .
ENV VITE_API_URL=/
ENV VITE_BASE_URL=/
RUN pnpm build

# ---- 运行阶段 ----
FROM nginx:alpine
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
