# 生产上线风险待办（方案B专用）

> 适用范围：当前仓库在 Railway 测试阶段与后续迁入公司代码仓阶段。  
> 状态定义：`todo` / `doing` / `done` / `blocked`。

| ID | 级别 | 风险项 | 影响 | 证据 | 整改动作 | 状态 | 验收标准 |
|---|---|---|---|---|---|---|---|
| P0-01 | P0 | 前端构建失败（Element Plus locale 类型冲突） | 无法发布前端镜像 | `frontend/src/main.ts` `frontend/src/App.vue` | 仅保留一处 locale 注入，修正类型定义 | done | `npm run build` 通过 |
| P0-02 | P0 | 方案B跳转页未实现（TODO） | 扫码后无法完成跳转 | `frontend/src/views/JumpPage.vue` | 接入公开落地接口，完成加载/跳转/兜底逻辑 | done | `/jump?qid=` 可渲染并可跳转 |
| P0-03 | P0 | 方案A/B并存导致链路混乱 | 线上行为不确定 | `backend/src/main/java/com/byd/qrcode/service/impl/QrcodeServiceImpl.java` | 后端改为仅方案B单路径，删除A字段/接口 | done | 代码与数据库不再出现A方案字段 |
| P0-04 | P0 | 配置接口返回敏感字段（appSecret/accessToken） | 凭据泄露 | `backend/src/main/java/com/byd/qrcode/controller/WechatConfigController.java` | 返回脱敏VO，不回传敏感字段 | done | 配置接口响应中不含敏感字段 |
| P0-05 | P0 | 高危清空接口可直接调用 | 数据被误删/恶意清空 | `backend/src/main/java/com/byd/qrcode/controller/ScanController.java` | 删除清空扫码记录接口 | done | `/api/scans` 删除接口不可用 |
| P1-01 | P1 | CORS 放开过宽 | 跨站调用风险 | `backend/src/main/java/com/byd/qrcode/config/WebConfig.java` | 改为环境变量白名单控制 | done | 可通过 `APP_CORS_ALLOWED_ORIGINS` 控制 |
| P1-02 | P1 | 异常全部HTTP 200 | 监控告警失真 | `backend/src/main/java/com/byd/qrcode/common/GlobalExceptionHandler.java` | 调整为4xx/5xx语义状态码 | done | 参数错误返回400，系统异常返回500 |
| P1-03 | P1 | 注册回传非幂等 | 转化统计被重复累计 | `backend/src/main/java/com/byd/qrcode/service/impl/ScanRecordServiceImpl.java` | 增加幂等判断，仅首次注册累计 | done | 同一 `scanId` 重放不重复加计数 |
| P1-04 | P1 | 生产配置与存储实现漂移（obs/minio） | 启动失败或运行偏差 | `backend/src/main/resources/application-prod.yml` | 生产配置与当前实现统一为 MinIO | done | `storage.type` 与实现一致 |
| P2-01 | P2 | 前端 `/api` 反代耦合 | 前后端分离部署困难 | `frontend/src/api/index.ts` `frontend/nginx.conf` | 改为 `VITE_API_BASE_URL`，去掉Nginx API反代依赖 | done | 前端可跨域直连后端服务 |
| P2-02 | P2 | 冗余代码未清理（未使用视图/临时文件） | 维护成本高 | `frontend/src/views/Architecture.vue` `frontend/vite.config.ts.timestamp-*` | 删除无用文件 | done | 仓库中不再存在上述文件 |
| P2-03 | P2 | 缺少测试与CI基线 | 回归风险高 | 全仓 | 迁入公司仓后补充CI、接口/链路测试 | todo | 合并门禁包含构建与关键用例 |

## 后续进入公司仓必须补齐
1. 接入公司统一认证（当前 Railway 测试阶段按“无鉴权”执行）。
2. 增加后端单元测试与集成测试，覆盖二维码生成、落地页、注册幂等、统计一致性。
3. 接入公司CI流水线并设置质量门禁。
