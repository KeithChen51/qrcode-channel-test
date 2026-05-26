# Production Risk TODO

> Scope: current simplified architecture, deployed as frontend + backend + MySQL.

| ID | Priority | Risk | Impact | Mitigation | Status | Acceptance |
|---|---|---|---|---|---|---|
| P0-01 | P0 | Frontend build failure | Cannot publish frontend image | Keep Element Plus locale injection consistent | done | `npm run build` passes |
| P0-02 | P0 | H5 jump page unavailable | Users cannot enter mini program after scan | Keep `/jump?qid=` flow backed by public landing API | done | `/jump?qid=` renders and can jump |
| P0-03 | P0 | Mixed QR flow variants | Online behavior unclear | Keep the backend on the H5 relay flow | done | Generated QR points to `jumpPageUrl` |
| P0-04 | P0 | Sensitive config fields exposed | Credential leak | Return desensitized config VO | done | Config APIs do not return secrets |
| P1-01 | P1 | CORS too broad | Cross-site request risk | Control origins with `APP_CORS_ALLOWED_ORIGINS` | done | Allowed origins are env-driven |
| P1-02 | P1 | QR image storage dependency | More services to deploy and operate | Generate PNG dynamically via `/api/qrcodes/{id}/image` | done | No object storage env vars or services are required |
| P1-03 | P1 | Missing backend regression coverage | Regression risk | Add controller tests for QR preview and download | doing | Backend tests run in a Java 21/Maven environment |
| P2-01 | P2 | Missing CI baseline | Manual release risk | Add build and key API tests in company CI | todo | Merge gate includes backend tests and frontend build |
