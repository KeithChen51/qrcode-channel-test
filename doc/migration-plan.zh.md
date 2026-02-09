
# 改造文档（Node/React → Spring Boot 2 + Vue 3）

本文档基于现有项目 qrcode-channel-demo ，
梳理代码结构与接口实现，
给出迁移到公司标准技术栈
（JDK8 + Spring Boot 2 / Vue3 + Element Plus）
的改造方案与落地步骤。

## 1. 项目概述
- 业务定位：地推渠道二维码生成、
  扫码统计、活动配置、
  回调对接（小程序后端）。
- 运行模式：单体服务同时提供 API 与前端页面；
  MySQL 持久化。
- 关键能力：微信小程序配置、
  二维码生成（方案 A/B）、
  扫码记录、统计报表、
  活动管理、H5 跳转页、
  回调接口。

## 2. 现状技术栈
- 后端：Node.js + TypeScript + Express + tRPC
- 数据库：MySQL + Drizzle ORM
- 前端：React + Vite + Tailwind + Radix UI
- 认证：OAuth 回调 + JWT Cookie
- 外部依赖：微信 API、存储代理（BUILT_IN_FORGE_*）、
  OAuth 服务

## 3. 目标技术栈
- 后端：Java 8 + Spring Boot 2
- 前端：Vue 3 + Element Plus
- 数据库：MySQL（建议配套 Flyway 或 Liquibase）
- 鉴权：对接公司统一认证（SSO）或保留 OAuth
  （待确认）
- 部署：Docker（NAS 测试环境）

## 4. 功能范围
- 用户认证：登录回调、会话校验、管理员权限
- 小程序配置：多配置管理、激活配置、
  token 缓存
- 二维码生成：方案 A/B、URL Link/URL Scheme、
  二维码图上传
- 扫码记录：记录、列表、统计、清空
- 活动管理：活动增删改查、样式配置
- 回调接口：小程序后端注册/查询回调
- JSSDK：签名与凭证校验
- H5 跳转页：方案 B 跳转页
- 系统接口：健康检查、通知（可选）

## 5. 现有接口清单（tRPC）
对应格式：/api/trpc/<router>.<procedure>。

### 5.1 基础/系统
- system.health
- system.notifyOwner

### 5.2 认证
- auth.me
- auth.logout

### 5.3 小程序配置
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

### 5.4 二维码
- qrcode.generate
- qrcode.batchGenerate
- qrcode.list
- qrcode.get
- qrcode.delete
- qrcode.batchDelete
- qrcode.getBase64
- qrcode.filterOptions

### 5.5 扫码记录
- scan.recordFromH5
- scan.record
- scan.registerUser
- scan.list
- scan.stats
- scan.filterOptions
- scan.clearAll
- scan.clearByConfig

### 5.6 二维码活动信息
- qrcodeInfo.getCampaignByQrcodeId

### 5.7 活动管理
- campaign.list
- campaign.getActive
- campaign.get
- campaign.create
- campaign.update
- campaign.delete

### 5.8 微信相关
- wechatJssdk.getSignature
- wechatJssdk.verifyCredentials
- wechatJssdk.getOriginalIdByQrcodeId

### 5.9 外部回调接口
- callback.registerUser
- callback.getScanRecord
- callback.batchGetScanRecords

### 5.10 非 tRPC 接口
- GET /api/oauth/callback（OAuth 回调）

## 6. 数据模型清单（MySQL）
表结构来源：drizzle/schema.ts。

1) users
- openId、role、lastSignedIn、createdAt、
  updatedAt 等

2) wechat_config
- 小程序配置：appId、appSecret、pagePath、
  defaultEnvVersion、accessToken 缓存

3) qrcode_records
- 二维码记录：configId/appId、storeId/staffId、schemeType、
  urlScheme/urlLink/jumpPageUrl、scanCount/registerCount、campaignId

4) scan_records
- 扫码记录：scanId、qrcodeId、门店/员工、活动、
  注册信息、IP/UA、时间

5) campaigns
- 活动配置：主题色、文案、背景图、
  有效期、状态

## 7. 页面清单（前端路由）
- /：Home
- /config：小程序配置
- /qrcode：二维码生成/管理
- /scan-test：扫码测试
- /architecture：架构说明
- /scan-dashboard：扫码统计看板
- /campaigns：活动管理
- /jump：H5 中转页
- /404：NotFound

备注：ComponentShowcase、ScanRecords
当前未挂载路由。

## 8. 外部依赖与配置项
- OAuth：VITE_OAUTH_PORTAL_URL / VITE_APP_ID
  / OAUTH_SERVER_URL / JWT_SECRET
- 业务存储：BUILT_IN_FORGE_API_URL
  / BUILT_IN_FORGE_API_KEY
- 微信 API：appId/appSecret，必要时
  WECHAT_API_PROXY
- 业务域名：APP_BASE_URL
  （用于生成 jumpPageUrl）

## 9. 改造策略
### 9.1 后端改造（Spring Boot 2）
1) 工程基础
   - 建立 Spring Boot 2 多环境配置
     （dev/test/prod）
   - 日志与异常统一处理
     （ControllerAdvice）

2) 数据库迁移
   - 用 Flyway/Liquibase 初始化表结构
     与索引
   - 迁移脚本对照 Drizzle SQL，
     确保字段一致

3) 认证与权限
   - 方案 A：对接公司 SSO
   - 方案 B：保留 OAuth + JWT Cookie
   - 建立 User 实体与管理员权限控制

4) API 改造
   - tRPC 迁 REST Controller
   - 定义请求/响应 DTO
   - 统一错误码与返回结构

5) 微信能力
   - access_token 缓存（DB 或 Redis）
   - 生成小程序码、URL Link/URL Scheme
   - JSSDK 签名接口

6) 二维码图片存储
   - 方案 A：NAS 本地文件系统
   - 方案 B：MinIO / S3 兼容
   - 替换现有 Forge 存储代理

### 9.2 前端改造（Vue 3 + Element Plus）
1) Vue 3 + Vite 脚手架
2) 路由：vue-router + 布局重构
3) UI 组件：Element Plus 替换 Tailwind/Radix
4) API 调用：axios/fetch，
   替换 tRPC client
5) 重点页面：二维码生成、统计、
   活动管理、H5 跳转页

### 9.3 REST 接口建议（示例）
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
- Qrcode
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
- Callback（对外）
  - POST /api/callback/register-user
  - GET /api/callback/scan-record
  - POST /api/callback/scan-records
- Wechat/JSSDK
  - GET /api/wechat/jssdk/signature
  - GET /api/wechat/jssdk/verify
  - GET /api/wechat/original-id?qrcodeId=...

## 10. 数据库迁移方案
1) 生成 Flyway/Liquibase 初始脚本
2) 对照字段类型与约束
3) 为查询场景增加索引
4) 用样例数据验证

## 11. 部署与环境配置
1) Docker 镜像
2) docker-compose 管理 app + mysql
3) 配置项分离（ENV 或配置中心）
4) NAS 挂载存储目录
   （使用本地存储时）

## 12. 测试与验收
1) API 单测与集成测试
2) 回调接口模拟（小程序后端）
3) 二维码生成与扫码链路
4) H5 跳转页兼容性测试
5) 统计一致性验证

## 13. 风险与待确认事项
- 认证方式（SSO 或 OAuth）
- 存储方案（NAS 本地或 MinIO）
- 微信代理策略
- 是否保留未挂载页面

## 14. 里程碑建议
1) 需求确认与方案评审
2) 后端基础与数据库迁移
3) 前端框架与核心页面
4) 关键业务链路联调
5) NAS 部署与测试
