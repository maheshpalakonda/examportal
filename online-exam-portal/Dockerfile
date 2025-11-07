# ---- build ----
FROM node:20 AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci || npm install
COPY . .
RUN npx ng build --configuration production || npm run build

# ---- serve ----
FROM nginx:stable
# If your Angular output is dist/online-exam-portal/browser, copy that exact path:
# COPY --from=build /app/dist/online-exam-portal/browser /usr/share/nginx/html
COPY --from=build /app/dist /usr/share/nginx/html
RUN printf 'server { \
  listen 80; \
  server_name _; \
  root /usr/share/nginx/html; \
  index index.html; \
  location / { try_files $$uri $$uri/ /index.html; } \
  location /api/ { \
    proxy_pass http://backend:8080/; \
    proxy_http_version 1.1; \
    proxy_set_header Host $$host; \
    proxy_set_header X-Real-IP $$remote_addr; \
    proxy_set_header X-Forwarded-For $$proxy_add_x_forwarded_for; \
    proxy_set_header X-Forwarded-Proto $$scheme; \
  } \
}' > /etc/nginx/conf.d/default.conf

