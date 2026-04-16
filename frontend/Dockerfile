# ─── Stage 1: сборка ────────────────────────────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# Сначала копируем только манифест зависимостей — слой кэшируется
# до тех пор, пока package.json не меняется
COPY package.json package-lock.json* ./
RUN npm ci --ignore-scripts

# Копируем исходники и собираем
COPY . .
RUN npm run build

# ─── Stage 2: раздача через nginx ───────────────────────────────────────────
FROM nginx:1.27-alpine

# Убираем дефолтный конфиг
RUN rm /etc/nginx/conf.d/default.conf

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
