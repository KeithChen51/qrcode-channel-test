# 本地开发启动（统一流程）

## 目标
- 仅保留一套本地启动方式，避免临时参数和多套命令并存。
- 仅保留一个业务健康检查接口：`GET /api/health`。

## 前置环境
- JDK 21（IDE 运行后端）
- Node.js 18+
- Docker Desktop（用于 MySQL + MinIO）

## 启动步骤
1. 在项目根目录启动基础依赖：
   - `docker compose up -d mysql minio minio-init`
2. 在 IDE 中启动后端主类：
   - 主类：`com.byd.qrcode.QrcodeChannelApplication`
   - Profile：`dev`
   - 端口：使用默认 `8080`（不要再额外覆盖端口）
3. 在 `frontend` 目录启动前端：
   - `npm run dev -- --host 127.0.0.1 --port 5173 --strictPort`

## 验证
- 后端健康检查：
  - `http://127.0.0.1:8080/api/health`
- 前端页面：
  - `http://127.0.0.1:5173`

## 约束
- 不再使用临时参数文件（例如 `backend/run-local.args`）作为标准启动方式。
- 不再依赖 `/actuator/health` 作为本项目健康检查入口。
