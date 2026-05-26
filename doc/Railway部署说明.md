# Railway 部署说明

当前主线已移除 MinIO / OBS 依赖。Railway 测试环境只需要：

1. MySQL
2. 后端 Spring Boot 服务
3. 前端 Nginx 静态站点

## 必要变量

```env
SPRING_PROFILES_ACTIVE=prod
DB_HOST=<mysql-host>
DB_PORT=3306
DB_NAME=qrcode_channel
DB_USERNAME=<db-user>
DB_PASSWORD=<db-password>
APP_BASE_URL=https://<your-domain>
APP_CORS_ALLOWED_ORIGINS=https://<your-domain>
WECHAT_MP_APPID=<mp-appid>
WECHAT_MP_SECRET=<mp-secret>
VITE_API_BASE_URL=/api
```

二维码图片由后端动态生成：

- 预览：`/api/qrcodes/{id}/image`
- 下载：`/api/qrcodes/{id}/image?download=true`
