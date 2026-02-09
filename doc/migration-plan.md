# Migration Plan (Node/React -> Spring Boot 2 + Vue 3)

This document describes the target architecture, scope, and implementation plan for migrating `qrcode-channel-demo` to the company stack (JDK8 + Spring Boot 2 / Vue 3 + Element Plus).

## 1. Project Summary
- Purpose: generate marketing QR codes, record scans, provide analytics, manage campaigns, and expose callback APIs for a mini-program backend.
- Current form: single service hosting APIs and frontend.
- Data store: MySQL.

## 2. Current Stack (Source)
- Backend: Node.js + TypeScript + Express + tRPC
- ORM: Drizzle (MySQL)
- Frontend: React + Vite + Tailwind + Radix UI
- Auth: OAuth callback + JWT cookie
- External dependencies: WeChat API, storage proxy (BUILT_IN_FORGE_*), OAuth service

## 3. Target Stack
- Backend: Java 8 + Spring Boot 2
- Frontend: Vue 3 + Element Plus
- DB: MySQL (suggest Flyway or Liquibase)
- Auth: company SSO or OAuth carry-over (to be confirmed)
- Deployment: Docker (NAS for test)

## 4. Functional Scope
- Auth: login callback, session validation, admin role
- WeChat config: multi-config, active config, token cache
- QR code: scheme A/B, URL Link/URL Scheme, image storage
- Scan records: capture, list, stats, cleanup
- Campaigns: CRUD, styling for H5 jump page
- Callback: register/query scan info for mini-program backend
- JSSDK: signature + credentials verify
- H5 jump page (scheme B)
- System: health + optional notification

## 5. Existing API Inventory (tRPC)
Format: `/api/trpc/<router>.<procedure>`.

### 5.1 System
- system.health
- system.notifyOwner

### 5.2 Auth
- auth.me
- auth.logout

### 5.3 WeChat Config
- config.list
- config.getActive
- config.get
- config.create
- config.update
- config.delete
- config.setActive
- config.test
- config.get_legacy
- config.save

### 5.4 QR Code
- qrcode.generate
- qrcode.batchGenerate
- qrcode.list
- qrcode.get
- qrcode.delete
- qrcode.batchDelete
- qrcode.getBase64
- qrcode.filterOptions

### 5.5 Scan Records
- scan.recordFromH5
- scan.record
- scan.registerUser
- scan.list
- scan.stats
- scan.filterOptions
- scan.clearAll
- scan.clearByConfig

### 5.6 QR Code Info
- qrcodeInfo.getCampaignByQrcodeId

### 5.7 Campaign
- campaign.list
- campaign.getActive
- campaign.get
- campaign.create
- campaign.update
- campaign.delete

### 5.8 WeChat JSSDK
- wechatJssdk.getSignature
- wechatJssdk.verifyCredentials
- wechatJssdk.getOriginalIdByQrcodeId

### 5.9 External Callback
- callback.registerUser
- callback.getScanRecord
- callback.batchGetScanRecords

### 5.10 Non-tRPC
- GET /api/oauth/callback

## 6. Data Models (MySQL)
Source: `drizzle/schema.ts`.

1) users  
- openId, role, lastSignedIn, createdAt, updatedAt, etc.

2) wechat_config  
- appId, appSecret, pagePath, defaultEnvVersion, accessToken cache.

3) qrcode_records  
- configId/appId, storeId/staffId, schemeType, urlScheme/urlLink/jumpPageUrl, scanCount/registerCount, campaignId.

4) scan_records  
- scanId, qrcodeId, store/staff info, campaign info, registration info, IP/UA, timestamps.

5) campaigns  
- theme color, copy, background image, time window, status.

## 7. Frontend Pages
- / : Home
- /config : WeChat config
- /qrcode : QR code management
- /scan-test : Scan test
- /architecture : Architecture notes
- /scan-dashboard : Scan analytics
- /campaigns : Campaign management
- /jump : H5 jump page
- /404 : NotFound

Note: ComponentShowcase and ScanRecords are not mounted in routing.

## 8. External Dependencies and Env
- OAuth: VITE_OAUTH_PORTAL_URL / VITE_APP_ID / OAUTH_SERVER_URL / JWT_SECRET
- Storage proxy: BUILT_IN_FORGE_API_URL / BUILT_IN_FORGE_API_KEY
- WeChat: appId/appSecret, optional WECHAT_API_PROXY
- Base URL: APP_BASE_URL (for jumpPageUrl)

## 9. Migration Strategy
### 9.1 Backend (Spring Boot 2)
1) Project bootstrap  
   - multi-profile config (dev/test/prod)  
   - unified error handling (ControllerAdvice)

2) DB migration  
   - generate initial schema via Flyway/Liquibase  
   - align indexes and constraints with Drizzle SQL

3) Auth  
   - Option A: integrate company SSO  
   - Option B: keep OAuth + JWT cookie  
   - ensure admin role check for restricted endpoints

4) API  
   - convert tRPC procedures to REST controllers  
   - define request/response DTOs  
   - consistent error codes and message schema

5) WeChat integration  
   - access_token cache in DB or Redis  
   - WxaCode, URL Link, URL Scheme generation  
   - JSSDK signature

6) QR code storage  
   - Option A: NAS local filesystem  
   - Option B: MinIO/S3 compatible  
   - replace existing Forge storage proxy

### 9.2 Frontend (Vue 3 + Element Plus)
1) Vue 3 + Vite setup  
2) Vue-router + layout refactor  
3) Replace Tailwind/Radix with Element Plus  
4) API client (axios/fetch) to match REST  
5) Focus pages: QR code, stats, campaigns, H5 jump page

### 9.3 REST Endpoint Proposal (Example)
- Auth  
  - GET /api/auth/me  
  - POST /api/auth/logout
- Config  
  - GET /api/wechat-configs  
  - GET /api/wechat-configs/active  
  - POST /api/wechat-configs  
  - PUT /api/wechat-configs/{id}  
  - DELETE /api/wechat-configs/{id}  
  - POST /api/wechat-configs/{id}/activate  
  - POST /api/wechat-configs/{id}/test
- QR Code  
  - POST /api/qrcodes  
  - POST /api/qrcodes/batch  
  - GET /api/qrcodes  
  - GET /api/qrcodes/{id}  
  - DELETE /api/qrcodes/{id}  
  - POST /api/qrcodes/batch-delete  
  - GET /api/qrcodes/base64?content=...
- Scan  
  - POST /api/scans/h5  
  - POST /api/scans  
  - POST /api/scans/register  
  - GET /api/scans  
  - GET /api/scans/stats  
  - GET /api/scans/filters  
  - DELETE /api/scans  
  - DELETE /api/scans/by-config/{configId}
- Campaign  
  - GET /api/campaigns  
  - GET /api/campaigns/active  
  - GET /api/campaigns/{id}  
  - POST /api/campaigns  
  - PUT /api/campaigns/{id}  
  - DELETE /api/campaigns/{id}
- Callback  
  - POST /api/callback/register-user  
  - GET /api/callback/scan-record  
  - POST /api/callback/scan-records
- WeChat/JSSDK  
  - GET /api/wechat/jssdk/signature  
  - GET /api/wechat/jssdk/verify  
  - GET /api/wechat/original-id?qrcodeId=...

## 10. DB Migration Plan
1) generate schema via Flyway/Liquibase  
2) confirm field types and constraints  
3) add indexes for scan query patterns  
4) validate with sample data

## 11. Deployment (NAS)
1) Docker image for backend  
2) docker-compose with MySQL  
3) env config mapping  
4) NAS volume for storage if local file storage

## 12. Testing Plan
- API unit + integration tests  
- callback simulation for mini-program backend  
- QR code generation and scan flow  
- H5 jump page compatibility  
- stats consistency checks

## 13. Risks / Open Items
- Auth integration decision (SSO vs OAuth).  
- Storage decision (NAS local vs MinIO).  
- WeChat proxy strategy.  
- Whether to keep unmounted pages.

## 14. Suggested Milestones
1) requirement confirmation and design review  
2) backend foundation + DB migration  
3) frontend scaffolding + core pages  
4) end-to-end integration  
5) NAS deployment and test

