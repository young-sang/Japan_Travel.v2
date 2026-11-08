# 화면 이미지 (D-085). Vite 로 빌드하고 nginx 가 내준다. /api 는 nginx.conf 가 백엔드로 넘긴다.
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
