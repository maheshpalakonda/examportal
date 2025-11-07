# ---- build ----
FROM node:20 AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci || npm install
COPY . .
RUN npx ng build --configuration production || npm run build

# ---- serve ----
FROM nginx:stable

# ✅ Correct path based on Angular output
COPY --from=build /app/dist/online-exam-portal /usr/share/nginx/html

RUN cat >/etc/nginx/conf.d/default.conf <<'NGINX'
server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://backend:8080/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
NGINX

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

