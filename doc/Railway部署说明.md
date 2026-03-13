# Railway 分离部署说明（方案B）

> 适用范围：Railway 历史测试环境。
> 主文档入口：`doc/从代码到落地部署指南.md`。公司环境部署请优先看主文档里的单机 Ubuntu 或 K8S 章节。

## 1. 部署拓扑
1. `frontend`：静态站点（Vite build + Nginx）
2. `backend`：Spring Boot API
3. `mysql`：MySQL 5.7+
4. `minio`：对象存储（测试阶段）

## 2. 环境变量

### 2.1 backend
- `SPRING_PROFILES_ACTIVE=prod`
- `DB_HOST=<mysql host>`
- `DB_PORT=<mysql port>`
- `DB_NAME=qrcode_channel`
- `DB_USERNAME=<db user>`
- `DB_PASSWORD=<db password>`
- `MINIO_ENDPOINT=<minio endpoint>`
- `MINIO_ACCESS_KEY=<minio access key>`
- `MINIO_SECRET_KEY=<minio secret key>`
- `MINIO_BUCKET=qrcode-images`
- `APP_BASE_URL=<frontend public url>`
- `APP_CORS_ALLOWED_ORIGINS=<frontend public url>`
- `WECHAT_MP_APPID=<wechat official-account appid>`
- `WECHAT_MP_SECRET=<wechat official-account appsecret>`

### 2.2 frontend（构建时变量）
- `VITE_API_BASE_URL=<backend public url>/api`

## 3. 启动顺序
1. 创建并初始化 `mysql`
2. 启动 `minio`，创建 `qrcode-images` bucket
3. 部署 `backend`
4. 部署 `frontend`

## 4. 冒烟用例
1. 打开前端首页，确认可加载配置列表与活动列表。
2. 在后台创建小程序配置并激活。
3. 创建活动并生成二维码。
4. 打开二维码对应 `/jump?qid=...`：
   - 页面样式正确
   - 点击按钮可跳转到 `urlLink`
5. 检查看板统计和扫码明细是否有新增数据。

## 5. 常见问题
1. **跳转地址仍是 localhost**  
检查 `APP_BASE_URL` 是否设置为前端公网域名。

2. **前端调用后端跨域失败**  
检查 `APP_CORS_ALLOWED_ORIGINS` 是否包含前端域名；多个域名使用逗号分隔。

3. **二维码图片无法访问**  
检查 MinIO bucket 权限与 `MINIO_*` 环境变量是否正确。
