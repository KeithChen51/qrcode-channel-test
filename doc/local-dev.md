# Local Development

## Goal
- Keep one standard local startup flow.
- Use `GET /api/health` as the application health check.
- Generate QR code images dynamically from the backend; no object storage service is required.

## Prerequisites
- JDK 21
- Node.js 18+
- Docker Desktop, only for MySQL

## Startup
1. Start the database from the project root:
   - `docker compose up -d mysql`
2. Start the backend from the IDE:
   - Main class: `com.byd.qrcode.QrcodeChannelApplication`
   - Profile: `dev`
   - Port: default `8080`
3. Start the frontend from `frontend`:
   - `npm run dev -- --host 127.0.0.1 --port 5173 --strictPort`

## Verification
- Backend health: `http://127.0.0.1:8080/api/health`
- Frontend: `http://127.0.0.1:5173`

## Constraints
- Do not use temporary parameter files such as `backend/run-local.args` as the standard startup path.
- Do not use `/actuator/health` as this project's health endpoint.
