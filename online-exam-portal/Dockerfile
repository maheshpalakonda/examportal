# -------------------------------
# Stage 1: Build Angular App
# -------------------------------
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci || npm install

COPY . .
RUN npm run build --prod || npx ng build --configuration production

# -------------------------------
# Stage 2: Serve with Nginx
# -------------------------------
FROM nginx:alpine

# ✅ Correct path for your Angular output folder
COPY --from=build /app/dist/online-exam-portal /usr/share/nginx/html/browser

# ✅ Custom Nginx configuration
RUN cat > /etc/nginx/conf.d/default.conf <<'NGINXCONF'
server {
    listen 80;
    server_name localhost;

    root /usr/share/nginx/html/browser;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # Proxy all /api requests to backend service
    location /api/ {
        proxy_pass http://examportal-backend:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    error_page 500 502 503 504 /50x.html;
    location = /50x.html {
        root /usr/share/nginx/html;
    }
}
NGINXCONF

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

