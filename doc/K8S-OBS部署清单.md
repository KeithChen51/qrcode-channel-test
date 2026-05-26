# 公司 K8S 落地清单

> 当前版本不再依赖 MinIO / OBS。二维码图片由后端根据 `jumpPageUrl` 动态生成，预览和下载都走 `GET /api/qrcodes/{id}/image`。

## 1. 服务组成

1. `frontend`：Vue 静态站点，Nginx 容器端口 `80`
2. `backend`：Spring Boot API，容器端口 `8080`
3. `mysql`：建议使用公司现有 MySQL

## 2. 镜像

1. 后端镜像：`backend/Dockerfile`
2. 前端镜像：`frontend/Dockerfile`
3. 建议标签：`<branch>-<short_sha>` 或 `<semver>-<short_sha>`

## 3. 必要环境变量

ConfigMap:

```env
SPRING_PROFILES_ACTIVE=prod
DB_HOST=<mysql-host>
DB_PORT=3306
DB_NAME=qrcode_channel
DB_USERNAME=<db-user>
APP_BASE_URL=https://qrcode.company.com
APP_CORS_ALLOWED_ORIGINS=https://qrcode.company.com
WECHAT_MP_APPID=<mp-appid>
```

Secret:

```env
DB_PASSWORD=<db-password>
WECHAT_MP_SECRET=<mp-secret>
```

## 4. QR 图片和下载

二维码图片不落对象存储。后端根据数据库中的 `jumpPageUrl` 实时生成 PNG：

- 预览：`GET /api/qrcodes/{id}/image`
- 下载：`GET /api/qrcodes/{id}/image?download=true`

下载接口会返回 `Content-Disposition: attachment; filename="qrcode-{id}.png"`。

## 5. K8S 资源

当前仓库保留最小资源清单：

- `deploy/k8s/base/namespace.yaml`
- `deploy/k8s/base/configmap.yaml`
- `deploy/k8s/base/secret.example.yaml`
- `deploy/k8s/base/backend-deployment.yaml`
- `deploy/k8s/base/backend-service.yaml`
- `deploy/k8s/base/frontend-deployment.yaml`
- `deploy/k8s/base/frontend-service.yaml`
- `deploy/k8s/base/ingress.yaml`
- `deploy/k8s/base/hpa.yaml`

部署前需要把镜像地址和域名替换为公司实际值。
