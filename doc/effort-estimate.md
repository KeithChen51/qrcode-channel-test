# Effort Estimate (Migration to Spring Boot 2 + Vue 3)

This document provides a detailed effort estimate for migrating `qrcode-channel-demo` to the company stack. All numbers are person-days (PD) for implementation and integration. Estimates include a P50 (most likely) and P90 (conservative) range.

## 1. Assumptions
- Functional scope remains the same as current system.
- No major UI redesign beyond framework replacement.
- MySQL stays as the database.
- NAS is the test environment for Docker deployment.
- Auth decision not finalized: estimate includes baseline OAuth carry-over; SSO integration is listed as an optional add-on.
- Storage decision not finalized: baseline keeps existing behavior via new storage adapter; MinIO/local storage is optional add-on.

## 2. Work Breakdown and Estimates

### 2.1 Backend (Spring Boot 2)
| Module | Scope | P50 (PD) | P90 (PD) |
| --- | --- | ---:| ---:|
| Project bootstrap | build, profiles, logging, error handling | 2 | 4 |
| DB migration | schema + indexes + data validation | 3 | 5 |
| Auth and role | OAuth carry-over or stub, admin guard | 3 | 5 |
| WeChat config | CRUD + token cache | 3 | 5 |
| QR code service | scheme A/B, URL Link/Scheme | 4 | 7 |
| Scan records | record, list, stats, filters, cleanup | 4 | 6 |
| Campaigns | CRUD + style data | 2 | 4 |
| Callback APIs | register + query | 2 | 3 |
| JSSDK | signature + verify | 2 | 3 |
| Storage adapter | NAS/local or MinIO abstraction | 2 | 4 |
| Integration tests | core API flows | 2 | 4 |
| **Backend subtotal** |  | **32** | **50** |

### 2.2 Frontend (Vue 3 + Element Plus)
| Module | Scope | P50 (PD) | P90 (PD) |
| --- | --- | ---:| ---:|
| Project bootstrap | Vue3 + Vite + router + layout | 2 | 4 |
| API client | axios, auth interceptor, error handling | 2 | 3 |
| WeChat config page | list/create/edit/activate/test | 3 | 5 |
| QR code page | generate, list, batch, preview | 5 | 8 |
| Scan dashboard | stats + charts | 3 | 5 |
| Campaign management | list/create/edit | 3 | 5 |
| H5 jump page | scheme B landing | 2 | 4 |
| Scan test page | manual scan flow | 2 | 3 |
| General pages | home, 404, architecture | 2 | 3 |
| Frontend tests | smoke + key flows | 2 | 3 |
| **Frontend subtotal** |  | **26** | **43** |

### 2.3 DevOps / Deployment
| Module | Scope | P50 (PD) | P90 (PD) |
| --- | --- | ---:| ---:|
| Docker build | backend image, env mapping | 2 | 3 |
| NAS compose | mysql + app, volume mount | 1 | 2 |
| CI/CD or release scripts | optional | 1 | 2 |
| **DevOps subtotal** |  | **4** | **7** |

### 2.4 QA / Integration
| Module | Scope | P50 (PD) | P90 (PD) |
| --- | --- | ---:| ---:|
| End-to-end testing | QR -> scan -> register -> stats | 3 | 5 |
| Callback verification | mini-program backend simulation | 2 | 3 |
| Regression and fixes | round-trip | 2 | 4 |
| **QA subtotal** |  | **7** | **12** |

### 2.5 Total (Baseline)
- P50 total: 32 + 26 + 4 + 7 = **69 PD**
- P90 total: 50 + 43 + 7 + 12 = **112 PD**

These totals assume parallel execution by backend and frontend engineers. If a single engineer performs all work, the calendar time will be longer due to sequence constraints.

## 3. Optional Add-ons and Deltas
- Company SSO integration: +5 to +10 PD
- MinIO or NAS local storage with signed URL: +3 to +6 PD
- UI redesign or new UX requirements: +5 to +12 PD
- New analytics/reporting features: +5 to +15 PD

## 4. Timeline Scenarios (Calendar)
Assume 5 PD per week per engineer.

1) **1 engineer (full stack)**  
   - P50: 69 PD -> ~14 weeks  
   - P90: 112 PD -> ~23 weeks

2) **2 engineers (BE + FE)**  
   - P50: ~8 to 10 weeks  
   - P90: ~12 to 16 weeks

3) **3 engineers (BE + FE + QA/DevOps)**  
   - P50: ~6 to 8 weeks  
   - P90: ~10 to 12 weeks

## 5. Critical Path
- DB migration and API design must start early.
- QR code generation and WeChat integration are on the backend critical path.
- Frontend depends on stable REST API schema.
- End-to-end flow testing depends on callback interfaces and storage adapter.

## 6. Deliverables
- Spring Boot 2 backend service with REST APIs.
- Vue 3 + Element Plus frontend.
- Flyway/Liquibase migration scripts.
- Docker image and docker-compose for NAS.
- Test report for main business flows.

## 7. Risks
- Auth integration scope changes mid-stream.
- Storage choice affects QR image handling and URL logic.
- WeChat API limits or proxy requirements.
- API compatibility for the existing mini-program backend.

## 8. Next Steps to Tighten the Estimate
- Confirm auth approach (SSO vs OAuth).
- Confirm storage approach (NAS local vs MinIO).
- Decide whether to keep unmounted pages and optional features.

