# 公司 K8S 落地清单（含华为云 OBS）

> 适用范围：公司 Kubernetes 环境的摘要清单。
> 主文档入口：`doc/从代码到落地部署指南.md`。如果要看完整部署流程，请先看主文档，这里主要保留 K8S 落地要点。

本文档基于当前项目结构整理，适合在公司 Kubernetes 环境中快速核对部署项，并说明如何接入华为云 OBS。

## 1. 部署拓扑

1. `frontend`：Vue 静态站点（Nginx，容器端口 `80`）
2. `backend`：Spring Boot API（容器端口 `8080`）
3. `mysql`：建议使用公司现有 MySQL（不建议先自建）
4. `obs`：华为云 OBS（外部服务，不部署在 K8S 内）

## 2. 镜像与构建

1. 后端镜像：`backend/Dockerfile`
2. 前端镜像：`frontend/Dockerfile`
3. 前端构建变量（必须）：
   - `VITE_API_BASE_URL=/api`（推荐）
4. 建议镜像命名：
   - `registry.example.com/qrcode-channel/backend:<tag>`
   - `registry.example.com/qrcode-channel/frontend:<tag>`

## 3. K8S 资源清单（最小集）

1. `Namespace`
2. `Secret`（敏感信息）
3. `ConfigMap`（非敏感配置）
4. `Deployment + Service`：backend
5. `Deployment + Service`：frontend
6. `Ingress`（TLS）
7. 可选：`HPA`

## 4. 路由规划（Ingress）

推荐单域名：

1. `https://qrcode.company.com/api` -> backend service:8080
2. `https://qrcode.company.com/` -> frontend service:80
3. `https://qrcode.company.com/jump` -> frontend service:80

这样前端可以用相对路径 `/api`，减少跨域复杂度。

## 5. 后端环境变量

```env
SPRING_PROFILES_ACTIVE=prod

DB_HOST=<mysql-host>
DB_PORT=3306
DB_NAME=qrcode_channel
DB_USERNAME=<db-user>
DB_PASSWORD=<db-password>

# 当前代码仅实现了 MinIO 适配器；接 OBS 请走 S3 兼容方式
STORAGE_TYPE=minio
MINIO_ENDPOINT=https://obs.<region>.myhuaweicloud.com
MINIO_ACCESS_KEY=<obs-ak>
MINIO_SECRET_KEY=<obs-sk>
MINIO_BUCKET=qrcode-images

APP_BASE_URL=https://qrcode.company.com
APP_CORS_ALLOWED_ORIGINS=https://qrcode.company.com

# 服务号（JS-SDK）
WECHAT_MP_APPID=<mp-appid>
WECHAT_MP_SECRET=<mp-secret>
```

## 6. 前端构建变量

```env
VITE_API_BASE_URL=/api
```

注意：这是构建时变量，修改后需要重新构建前端镜像并发布。

## 7. OBS 接入要点（关键）

1. 当前代码没有独立 OBS 实现，仅有 `MinioStorageService`。
2. 因此接 OBS 时仍需配置 `STORAGE_TYPE=minio` + OBS 的 endpoint/ak/sk。
3. `MINIO_ENDPOINT` 同时用于上传和返回外链 URL，必须填写用户可访问域名。
4. 需提前创建 bucket（例如 `qrcode-images`）。
5. 需配置对象可读策略（或后续改为签名 URL）。

## 8. 数据库初始化

1. 首次部署执行：`sql/init.sql`
2. 在后台创建并激活 `wechat_config`（`app_id/app_secret/original_id/page_path`）。

## 9. 健康检查与探针

1. backend 探针：`GET /api/health`（端口 `8080`）
2. frontend 探针：`GET /`（端口 `80`）

## 10. 微信侧配置

1. JS 安全域名：配置前端域名（如 `qrcode.company.com`）
2. 校验文件：放在前端根路径（`frontend/public/MP_verify_xxx.txt`）
3. 服务号 IP 白名单：需放行后端出口 IP（否则会报 `invalid ip`）

## 11. 验收清单

1. `GET /api/health` 返回 200
2. 前端页面可访问，配置管理可读写
3. 可生成二维码，详情可见 `jumpPageUrl/urlLink/qrcodeUrl`
4. 扫码后“扫码看板”新增记录
5. 二维码图片链接可直接访问

## 12. 风险与后续建议

1. 当前对象存储实现将 `MINIO_ENDPOINT` 直接拼接为外链 URL，建议后续增加 `STORAGE_PUBLIC_ENDPOINT` 以解耦“上传地址”和“公网访问地址”。
2. 当前数据库初始化依赖 SQL 文件，建议后续引入 Flyway/Liquibase 做版本化迁移。
3. 建议补充标准 K8S 模板：`Deployment/Service/Ingress/ConfigMap/Secret/HPA`，纳入公司 CI/CD。

## 13. 从本地项目到 CI/CD 要处理什么（落地清单）

1. 代码与分支规范
   - 统一以 Git 仓库作为发布源头，约定 `main`（生产）、`develop`（联调）、`feature/*`（开发）。
   - 约定触发规则：`feature/*` 触发 CI；`main` 合并后触发生产 CD。
2. 构建可重复
   - 前端保留并提交 `frontend/package-lock.json`（已存在）。
   - 后端固定 Maven/Java 版本（当前 `Java 21`，见 `backend/pom.xml`）。
3. 质量门禁（至少要有）
   - 前端：`npm ci && npm run build`。
   - 后端：`mvn -B clean test`（至少跑编译与测试，当前即使无测试用例也能先作为门禁骨架）。
4. 镜像制品
   - 使用现有 `backend/Dockerfile` 和 `frontend/Dockerfile` 构建镜像。
   - 统一标签：`<branch>-<short_sha>` 或 `<semver>-<short_sha>`，避免覆盖旧镜像。
5. 配置与密钥分离
   - `ConfigMap`：`APP_BASE_URL`、`APP_CORS_ALLOWED_ORIGINS`、`MINIO_ENDPOINT`、`MINIO_BUCKET` 等非敏感配置。
   - `Secret`：`DB_PASSWORD`、`MINIO_ACCESS_KEY`、`MINIO_SECRET_KEY`、`WECHAT_MP_SECRET` 等敏感信息。
6. 数据库变更流程
   - 短期：发布前执行 `sql/init.sql` 与 `sql/migrations/*.sql`。
   - 中期：迁移到 Flyway/Liquibase，纳入 CI 校验。
7. K8S 部署与回滚
   - CD 更新 Deployment 镜像标签后执行滚动发布。
   - 保留前一版本镜像标签，发布异常时执行回滚（`kubectl rollout undo`）。
8. 发布后验收
   - 接口健康：`GET /api/health`。
   - 业务链路：生成二维码 -> 扫码跳转 -> 看板新增记录。

## 14. 建议仓库目录（CI/CD 相关）

```text
.
|-- backend/
|-- frontend/
|-- sql/
|-- doc/
|-- deploy/
|   |-- k8s/
|   |   |-- base/
|   |   |   |-- backend-deployment.yaml
|   |   |   |-- backend-service.yaml
|   |   |   |-- frontend-deployment.yaml
|   |   |   |-- frontend-service.yaml
|   |   |   |-- ingress.yaml
|   |   |-- overlays/
|   |       |-- dev/
|   |       |-- staging/
|   |       |-- prod/
|-- .gitee/
|   |-- workflows/
|       |-- ci.yml
|       |-- cd-dev.yml
|       |-- cd-prod.yml
```

说明：若公司使用 Jenkins，保留 `deploy/k8s` 目录，改为由 Gitee Webhook 触发 Jenkins Pipeline 也可。

## 15. 最小流水线示例（Gitee + K8S，伪代码）

```yaml
pipeline:
  name: qrcode-channel-cicd
  trigger:
    - push: [feature/*, develop, main]
    - pull_request: [main]

  stages:
    - name: ci
      jobs:
        - name: frontend-build
          script:
            - cd frontend
            - npm ci
            - npm run build
        - name: backend-test
          script:
            - cd backend
            - mvn -B clean test

    - name: cd-prod
      only: [main]
      needs: [ci]
      jobs:
        - name: docker-build-push
          script:
            - export IMAGE_TAG=${GITEE_COMMIT:0:7}
            - docker build -t registry.example.com/qrcode-channel/backend:${IMAGE_TAG} backend
            - docker build --build-arg VITE_API_BASE_URL=/api -t registry.example.com/qrcode-channel/frontend:${IMAGE_TAG} frontend
            - docker push registry.example.com/qrcode-channel/backend:${IMAGE_TAG}
            - docker push registry.example.com/qrcode-channel/frontend:${IMAGE_TAG}
        - name: deploy-k8s
          script:
            - kubectl -n qrcode set image deploy/backend backend=registry.example.com/qrcode-channel/backend:${IMAGE_TAG}
            - kubectl -n qrcode set image deploy/frontend frontend=registry.example.com/qrcode-channel/frontend:${IMAGE_TAG}
            - kubectl -n qrcode rollout status deploy/backend
            - kubectl -n qrcode rollout status deploy/frontend
```

说明：以上为“流程伪代码”，实际字段请按你们 Gitee 企业版流水线模板填写（核心逻辑不变）。

## 16. CI/CD 变量与凭据清单（最小集）

1. 镜像仓库
   - `REGISTRY_SERVER`
   - `REGISTRY_USERNAME`
   - `REGISTRY_PASSWORD`
2. K8S 访问
   - `KUBE_CONFIG`（或 OIDC + RBAC）
   - `K8S_NAMESPACE=qrcode`
3. 业务密钥（部署到集群 Secret）
   - `DB_PASSWORD`
   - `MINIO_ACCESS_KEY`
   - `MINIO_SECRET_KEY`
   - `WECHAT_MP_SECRET`
