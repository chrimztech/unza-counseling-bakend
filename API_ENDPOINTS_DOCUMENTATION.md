# UNZA Counseling Management System — API Documentation

**Server context path:** `/api` (set via `server.servlet.context-path=/api` in `application.properties`)
**WebSocket:** STOMP over `/api/ws`, application prefix `/app`, broker prefixes `/topic` and `/queue`
**Auth:** stateless JWT (`Authorization: Bearer <token>`), issued by `POST /auth/login`, `/auth/register`, or `/auth/anonymous-login`. Method-level authorization via Spring `@PreAuthorize` (`@EnableMethodSecurity`).
**Roles:** `ADMIN`, `SUPER_ADMIN`, `COUNSELOR`, `STUDENT`, `CLIENT`, `SECURITY` (Spring's `hasRole('X')` / `hasAnyRole('X','Y')` automatically prepend `ROLE_` — annotations must use the bare role name, e.g. `hasAnyRole('ADMIN','COUNSELOR')`, never `hasAnyRole('ROLE_ADMIN', ...)`, which checks for a nonexistent authority and always fails).

This document was regenerated in full — every one of the 42 `@RestController` classes under `src/main/java/zm/unza/counseling/controller` is covered below, along with the eight authorization fixes made alongside this update (see "Recent Security Fixes" below).

---

## ⚠️ Route-doubling gotcha (read this first)

Most controllers declare `@RequestMapping` with an array of legacy path variants, typically:

```java
@RequestMapping({"/api/v1/x", "/api/x", "/v1/x", "/x"})
```

Because `server.servlet.context-path=/api` already prepends `/api` to **every** mapping, only two of those four patterns ever resolve to a route Spring will actually match against an incoming request:

- `/x` → resolves to real route `/api/x` ✅
- `/v1/x` → resolves to real route `/api/v1/x` ✅
- `/api/x` → resolves to dead route `/api/api/x` ❌ (never reachable)
- `/api/v1/x` → resolves to dead route `/api/api/v1/x` ❌ (never reachable)

So despite what the annotation array seems to offer, **the only real base paths are `/api/<resource>` and `/api/v1/<resource>`** for any controller using this four-pattern array. This is dead weight in the annotations, not a functional problem for the two real routes — but if you're calling the API and something 404s, check whether you accidentally used a doubled `/api/api/...` path copied from the annotation literally. Two frontend call sites that had this exact bug were fixed on the frontend side (not documented here — see the frontend repo's own fix notes).

The endpoint tables below list paths **relative to the controller's mapping** (e.g. `POST /clients`) — prepend `/api` or `/api/v1` per the rule above to get the real, reachable route.

---

## Recent Security Fixes (this pass)

| # | Area | Problem | Fix |
|---|------|---------|-----|
| 1 | `CrisisAlertController` | Class-level `@PreAuthorize("hasAnyRole('ROLE_COUNSELOR','ROLE_ADMIN','ROLE_SUPER_ADMIN')")` checked for the nonexistent authority `ROLE_ROLE_COUNSELOR` etc. (Spring auto-prepends `ROLE_`) — the entire controller was unreachable by anyone. | Changed to `hasAnyRole('COUNSELOR','ADMIN','SUPER_ADMIN')`, matching the rest of the codebase's convention. |
| 2 | `ClientController` | `POST /clients` (create) and `GET /clients` (list/search) had no `@PreAuthorize` — any authenticated role, including STUDENT/CLIENT, could create client records or browse the full roster. | Added `@PreAuthorize("hasAnyRole('ADMIN','COUNSELOR')")` to both, matching the controller's other endpoints. |
| 3 | `ClientIntakeFormController`, `PersonalDataFormController` | No `@PreAuthorize` anywhere — zero authorization on client intake/personal data CRUD. | Added class-level `@PreAuthorize("hasAnyRole('ADMIN','COUNSELOR')")` to both. Confirmed via the web (`ClientIntakeFormsScreen.tsx` under `src/screens/counselor/`) and mobile (`client_intake_forms_screen.dart`) frontends that these forms are always filled in by a counselor/admin during/after a session — no CLIENT/STUDENT caller submits their own intake or personal data record directly, so a blanket staff-only restriction is correct (no per-endpoint self-service carve-out needed). |
| 4 | `ConsentFormController` | No `@PreAuthorize` anywhere. | Per-endpoint treatment: management endpoints (`POST/PUT/DELETE /forms`, `/forms/{id}/activate`, `/forms/{id}/deactivate`, `GET /forms` list-all, `GET /statistics`) → `hasAnyRole('ADMIN','COUNSELOR')`. Self-service/read endpoints bound to the caller's own `Principal` (`GET /forms/{id}`, `GET /forms/active`, `GET /forms/latest`, `POST /sign`, `GET /check-signed`, `GET /history`) → `isAuthenticated()`, so any authenticated user — including anonymous-session users who hold a JWT from `/auth/anonymous-login` — can sign/check/view their own consent history. |
| 5 | IDOR: `BookmarkController`, `KeyboardShortcutController`, `DashboardConfigController`, `ChatController`, `FeedbackController /my` | All trusted a caller-supplied `userId` request param/path variable instead of the authenticated principal — any user could read or write another user's bookmarks, shortcuts, dashboard layout, AI chat history, or feedback just by passing a different id. | All five now derive the user id from `Authentication`/`Principal` via `UserService.getUserByEmail(authentication.getName())` (the same pattern already used by `ConsentFormController` and `UserController#getCurrentUserProfile`). `Bookmark`/`KeyboardShortcut` `{id}`-scoped update/delete endpoints additionally now verify the resource's owning `userId` matches the caller before allowing the operation. |
| 6 | Anonymous booking flow | `SecurityConfig` had `permitAll` rules for `/counselors/available` and `/counselors/*/availability` — neither route exists in `CounselorController` (dead config), and no live frontend call site (`getAvailableCounselorsAxios` in `apiService.ts` is defined but never called; `AppointmentForm.tsx`, the only component that calls `checkAvailability`, is not mounted anywhere in the routed app). Separately, `AppointmentController`'s `/appointments/availability` and `/appointments/stats` were `permitAll` at the filter-chain level but still carried `hasAnyRole(...)` `@PreAuthorize`, so a genuinely unauthenticated caller (Spring's anonymous principal has no application role) still got 403 even though the config claimed these were public. | Removed the dead `SecurityConfig` rules for the nonexistent counselor-availability routes. Changed `/appointments/availability` and `/appointments/stats` `@PreAuthorize` to `permitAll()` so the filter-chain's public intent is actually honored (aggregate stats and slot lookups carry no PII). |
| 7 | `ClinicController` webhook auth inconsistency | `/clinic/visits/inbound` required an ADMIN/COUNSELOR JWT while its sibling `/clinic/security-alerts/inbound*` endpoints were gated by an `X-Service-Api-Key` header and `permitAll`. Same external clinic system, two different auth mechanisms. | `/clinic/visits/inbound` now uses the same `X-Service-Api-Key` pattern (checked against `app.cross-system.api-key`) and is `permitAll` in `SecurityConfig`, matching its sibling. |
| 8 | `MentalHealthAcademicAnalysisController` | `DELETE /{id}` only called `service.getAnalysisById(id)` (a fetch) and never actually deleted the record. | Added `MentalHealthAcademicAnalysisService.deleteAnalysis(id)` (fetch-or-throw + `repository.delete(...)`) and wired the controller to call it. |

---

## Controller Index

1. [AcademicPerformanceController](#1-academicperformancecontroller) — `/academic-performance`
2. [AdminController](#2-admincontroller) — `/admin`
3. [AnalyticsController](#3-analyticscontroller) — `/analytics`
4. [AnonymousActivityController](#4-anonymousactivitycontroller) — `/admin/anonymous-activity`
5. [AppointmentController](#5-appointmentcontroller) — `/appointments`
6. [AssessmentController](#6-assessmentcontroller) — `/assessments`
7. [AuditTrailController](#7-audittrailcontroller) — `/audit`
8. [AuthController](#8-authcontroller) — `/auth`
9. [BookmarkController](#9-bookmarkcontroller) — `/bookmarks`
10. [CaseController](#10-casecontroller) — `/cases`
11. [CaseDocumentController](#11-casedocumentcontroller) — `/cases/documents`
12. [ChatController](#12-chatcontroller) — `/ai-chat`
13. [ClientController](#13-clientcontroller) — `/clients`
14. [ClientIntakeFormController](#14-clientintakeformcontroller) — `/client-intake-forms`
15. [ClinicController](#15-cliniccontroller) — `/clinic`
16. [ConsentFormController](#16-consentformcontroller) — `/consent`
17. [ContactsController](#17-contactscontroller) — `/contacts`
18. [ConversationsController](#18-conversationscontroller) — `/conversations`
19. [CounselorController](#19-counselorcontroller) — `/counselors`
20. [CrisisAlertController](#20-crisisalertcontroller) — `/crisis-alerts`
21. [DashboardConfigController](#21-dashboardconfigcontroller) — `/dashboard/config`
22. [DashboardController](#22-dashboardcontroller) — `/dashboard`
23. [FeedbackController](#23-feedbackcontroller) — `/feedback`
24. [GoalController](#24-goalcontroller) — `/goals`
25. [HealthController](#25-healthcontroller) — `/health`
26. [KeyboardShortcutController](#26-keyboardshortcutcontroller) — `/keyboard-shortcuts`
27. [KnowledgeBaseController](#27-knowledgebasecontroller) — `/knowledge-base`
28. [MentalHealthAcademicAnalysisController](#28-mentalhealthacademicanalysiscontroller) — `/analysis`
29. [MessageController](#29-messagecontroller) — `/messages`
30. [NotificationController](#30-notificationcontroller) — `/notifications`
31. [PersonalDataFormController](#31-personaldataformcontroller) — `/personal-data-forms`
32. [ReportController](#32-reportcontroller) — `/reports`
33. [ResourceController](#33-resourcecontroller) — `/resources`
34. [RiskAssessmentController](#34-riskassessmentcontroller) — `/risk-assessments`
35. [ScholarshipController](#35-scholarshipcontroller) — `/scholarships`
36. [SecurityAlertController](#36-securityalertcontroller) — `/security-alerts`
37. [SelfAssessmentController](#37-selfassessmentcontroller) — `/self-assessments`
38. [SessionController](#38-sessioncontroller) — `/sessions`
39. [SessionNoteController](#39-sessionnotecontroller) — `/sessions` (notes sub-paths)
40. [SettingsController](#40-settingscontroller) — `/settings`
41. [UserController](#41-usercontroller) — `/users`
42. [WebSocketEventController](#42-websocketeventcontroller) — STOMP `@MessageMapping`

---

### 1. AcademicPerformanceController
Base: `/academic-performance`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/` | ADMIN, COUNSELOR | Create academic performance record |
| GET | `/{id}` | ADMIN, COUNSELOR, CLIENT | Get record by id |
| GET | `/client/{clientId}` | ADMIN, COUNSELOR, CLIENT | All records for a client |
| GET | `/client/{clientId}/paginated` | ADMIN, COUNSELOR, CLIENT | Paginated records for a client |
| GET | `/client/{clientId}/latest` | ADMIN, COUNSELOR, CLIENT | Latest record for a client |
| GET | `/client/{clientId}/summary` | ADMIN, COUNSELOR, CLIENT | Summary stats for a client |
| GET | `/client/{clientId}/gpa-trend` | ADMIN, COUNSELOR, CLIENT | GPA trend data |
| PUT | `/{id}` | ADMIN, COUNSELOR | Update record |
| DELETE | `/{id}` | ADMIN | Delete record |
| GET | `/at-risk` | ADMIN, COUNSELOR | Students flagged at-risk academically |
| GET | `/low-gpa` | ADMIN, COUNSELOR | Students below GPA threshold |
| GET | `/faculty/{faculty}` | ADMIN, COUNSELOR | Records by faculty |
| GET | `/statistics` | ADMIN, COUNSELOR | Aggregate statistics |
| GET | `/analytics` | ADMIN, COUNSELOR | Analytics view |
| POST | `/sync/sis` | ADMIN, COUNSELOR, CLIENT | Bulk sync results from Student Information System |
| POST | `/client/{clientId}/sync/sis` | ADMIN, COUNSELOR, CLIENT | Sync one client's SIS results |
| GET | `/client/{clientId}/cached/sis` | ADMIN, COUNSELOR, CLIENT | Read cached SIS sync results |
| GET | `/sis/student/{studentId}` | ADMIN, COUNSELOR | SIS results by student id |
| GET | `/client/{clientId}/sis-results` | ADMIN, COUNSELOR | SIS results by client id |

### 2. AdminController
Base: `/admin`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/users` | ADMIN | List admin users |
| POST | `/` | ADMIN | Create an admin user |
| DELETE | `/{id}` | SUPER_ADMIN | Delete an admin user |
| GET | `/messages/audit` | ADMIN | Message audit log |
| GET | `/messages/audit/stats` | ADMIN | Message audit stats |

### 3. AnalyticsController
Base: `/analytics`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/overview` | ADMIN, COUNSELOR | Dashboard-style analytics overview |
| GET | `/intervention-report` | ADMIN, COUNSELOR | Intervention report |
| GET | `/counselor-performance` | ADMIN, COUNSELOR | Counselor performance analytics |
| GET | `/client-demographics` | ADMIN, COUNSELOR | Client demographics breakdown |
| GET | `/session-analytics` | ADMIN, COUNSELOR | Session analytics |
| GET | `/risk-assessment` | ADMIN, COUNSELOR | Risk assessment analytics |
| GET | `/time-analysis` | ADMIN, COUNSELOR | Time-based analysis |
| GET | `/outcomes` | ADMIN, COUNSELOR | Outcomes analytics |
| GET | `/export` | ADMIN | Export analytics (csv/etc.) |

### 4. AnonymousActivityController
Base: `/admin/anonymous-activity`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/` | ADMIN | List anonymous-session activity |
| GET | `/stats` | ADMIN | Anonymous activity stats |

### 5. AppointmentController
Base: `/appointments`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/` | authenticated | List appointments (paginated) |
| GET | `/{id}` | authenticated | Get appointment by id |
| POST | `/` | ADMIN, COUNSELOR, STUDENT, CLIENT | Create appointment |
| PUT | `/{id}` | ADMIN, COUNSELOR, STUDENT, CLIENT | Update appointment |
| PUT | `/{id}/status` | ADMIN, COUNSELOR, STUDENT, CLIENT | Update status |
| DELETE | `/{id}` | ADMIN, COUNSELOR | Delete appointment |
| GET | `/client/{clientId}` | authenticated | Appointments for a client (paginated) |
| GET | `/client/{clientId}/all` | authenticated | All appointments for a client |
| GET | `/student/{studentId}` | authenticated | Appointments by student id |
| GET | `/counselor/{counselorId}` | authenticated | Appointments by counselor |
| GET | `/today` | ADMIN, COUNSELOR, STUDENT, CLIENT | Today's appointments |
| GET | `/upcoming` | ADMIN, COUNSELOR, STUDENT, CLIENT | Upcoming appointments |
| GET | `/past` | ADMIN, COUNSELOR, STUDENT, CLIENT | Past appointments |
| GET | `/pending` | ADMIN, COUNSELOR, STUDENT, CLIENT | Pending appointments |
| GET | `/confirmed` | ADMIN, COUNSELOR, STUDENT, CLIENT | Confirmed appointments |
| GET | `/cancelled` | ADMIN, COUNSELOR | Cancelled appointments |
| PUT | `/{id}/cancel` | ADMIN, COUNSELOR, STUDENT, CLIENT | Cancel appointment |
| PUT | `/{id}/confirm` | ADMIN, COUNSELOR | Confirm appointment |
| PUT | `/{id}/reschedule` | ADMIN, COUNSELOR, STUDENT, CLIENT | Reschedule appointment |
| GET | `/unassigned` | ADMIN, COUNSELOR | Unassigned appointments |
| GET | `/unassigned/count` | ADMIN, COUNSELOR | Count of unassigned appointments |
| GET | `/availability` | **public** (`permitAll()`) | Check counselor availability slots — fixed in item 6, see notes above |
| GET | `/stats` | **public** (`permitAll()`) | Aggregate appointment stats — fixed in item 6, see notes above |
| GET | `/export` | ADMIN | Export appointments |
| POST | `/admin/assign` | ADMIN | Admin assigns a counselor to a session |
| POST | `/counselor/take/{appointmentId}` | COUNSELOR | Counselor self-assigns an unassigned appointment |

Also `permitAll` at the `SecurityConfig` filter-chain level: `/appointments/availability`, `/appointments/stats` (see fix #6).

### 6. AssessmentController
Base: `/assessments`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/self` | STUDENT, CLIENT, COUNSELOR, ADMIN | List self-assessments |
| POST | `/self/submit` | STUDENT, CLIENT | Submit a self-assessment |
| POST | `/risk` | COUNSELOR, ADMIN | Create a risk assessment |
| GET | `/risk/client/{clientId}` | COUNSELOR, ADMIN | Risk assessments for a client |

### 7. AuditTrailController
Base: `/audit`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/` | ADMIN | List audit logs |
| GET | `/user/{userId}` | ADMIN | Audit logs for a user |
| GET | `/range` | ADMIN | Audit logs in a date range |
| GET | `/failed` | ADMIN | Failed action logs |
| GET | `/search` | ADMIN | Search audit logs |
| GET | `/entity` | ADMIN | Audit logs for an entity |

### 8. AuthController
Base: `/auth`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/login` | public | Login, returns JWT |
| POST | `/register` | public | Register a new account |
| POST | `/logout` | authenticated | Logout current session |
| POST | `/anonymous-login` | public | Start an anonymous session (issues a JWT with `ROLE_CLIENT`, `isAnonymous: true`) |
| GET | `/profile` | authenticated | Current user profile |
| POST | `/refresh` | public (refresh token in body) | Refresh access token |
| POST | `/logout-all` | authenticated | Logout all devices |
| POST | `/password-reset-request` | public | Request password reset email |
| POST | `/password-reset` | public | Reset password with token |
| POST | `/verify-email` | public | Verify email with token |
| POST | `/resend-verification` | public | Resend verification email |
| GET | `/validate-token` | public (token in header) | Validate a JWT |
| GET | `/permissions` | authenticated | Current user's permissions |
| POST | `/2fa/enable` | authenticated | Enable 2FA |
| POST | `/2fa/verify` | authenticated | Verify 2FA code |
| POST | `/2fa/disable` | authenticated | Disable 2FA |

All of `/auth/**` is `permitAll` at the `SecurityConfig` filter-chain level; individual endpoints that need a logged-in user rely on the JWT filter having already populated the `SecurityContext`, not on `@PreAuthorize`.

### 9. BookmarkController
Base: `/bookmarks`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/` | authenticated | Add a bookmark (owner = caller) |
| GET | `/` | authenticated | List **own** bookmarks — fixed in item 5 (was `?userId=` IDOR) |
| GET | `/frequent` | authenticated | Own frequently-used bookmarks |
| GET | `/category/{category}` | authenticated | Own bookmarks by category |
| POST | `/{id}/use` | authenticated, owner-checked | Increment usage count |
| PUT | `/{id}` | authenticated, owner-checked | Update a bookmark |
| DELETE | `/{id}` | authenticated, owner-checked | Delete a bookmark |

### 10. CaseController
Base: `/cases`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/` | ADMIN, COUNSELOR | Create case |
| GET | `/{id}` | ADMIN, COUNSELOR | Get case by id |
| GET | `/{caseId}/appointments` | authenticated | Appointments linked to a case |
| GET | `/number/{caseNumber}` | ADMIN, COUNSELOR | Get case by case number |
| GET | `/client/{clientId}` | ADMIN, COUNSELOR | Cases for a client |
| GET | `/counselor/{counselorId}` | ADMIN, COUNSELOR | Cases for a counselor |
| GET | `/` | ADMIN, COUNSELOR | List/filter all cases |
| PUT | `/{id}` | ADMIN, COUNSELOR | Update case |
| PATCH | `/{id}/status` | ADMIN, COUNSELOR | Update case status |
| PATCH | `/{id}/priority` | ADMIN, COUNSELOR | Update case priority |
| DELETE | `/{id}` | ADMIN, COUNSELOR | Delete case |
| POST | `/assign` | ADMIN, COUNSELOR | Assign counselor to case |
| GET | `/{caseId}/assignments` | ADMIN, COUNSELOR | Assignment history |
| GET | `/counselor/{counselorId}/active-assignments` | ADMIN, COUNSELOR | Active assignments for a counselor |
| GET | `/stats` | ADMIN, COUNSELOR | Case stats |
| GET | `/stats/counselor` | ADMIN, COUNSELOR | Case stats by counselor |

### 11. CaseDocumentController
Base: `/cases/documents`, class-level `@PreAuthorize("hasAnyRole('ADMIN','COUNSELOR')")`

| Method | Path | Purpose |
|---|---|---|
| POST | `/` | Upload document |
| GET | `/{id}` | Get document by id |
| GET | `/case/{caseId}` | Documents for a case |
| GET | `/case/{caseId}/public` | Public documents for a case |
| GET | `/uploaded-by/{uploadedBy}` | Documents by uploader |
| GET | `/search` | Search documents by case + filename |
| DELETE | `/{id}` | Delete a document |
| DELETE | `/case/{caseId}` | Delete all documents for a case |

### 12. ChatController (AI chat)
Base: `/ai-chat`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/history/{userId}` | authenticated | **Own** AI chat history — fixed in item 5 (path `userId` is now ignored; id derived from principal) |
| POST | `/message` | authenticated | Send an AI chat message (owner = caller) |
| DELETE | `/history/{userId}` | authenticated | Clear **own** AI chat history |

### 13. ClientController
Base: `/clients`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/` | ADMIN, COUNSELOR | Create client (+ optional case) — **fixed in item 2**, was unrestricted |
| GET | `/` | ADMIN, COUNSELOR | List/search/filter clients — **fixed in item 2**, was unrestricted |
| GET | `/{id}` | ADMIN, COUNSELOR, or self (`#id == authentication.principal.id`) | Get client by id |
| GET | `/student/{studentId}` | ADMIN, COUNSELOR | Get client by student id |
| PUT | `/{id}` | ADMIN, or self | Update client |
| GET | `/stats` | ADMIN, COUNSELOR | Client stats |
| PUT | `/{id}/risk-level` | ADMIN, COUNSELOR | Update risk level |

### 14. ClientIntakeFormController
Base: `/client-intake-forms`, class-level `@PreAuthorize("hasAnyRole('ADMIN','COUNSELOR')")` — **fixed in item 3**, previously unrestricted

| Method | Path | Purpose |
|---|---|---|
| POST | `/clients/{clientId}` | Create intake form for a client |
| GET | `/{id}` | Get by id |
| GET | `/clients/{clientId}` | Get by client id |
| GET | `/cases/{caseId}` | Get by case id |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |

### 15. ClinicController
Base: `/clinic`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/referrals` | ADMIN, COUNSELOR | Create clinic referral |
| GET | `/referrals` | ADMIN, COUNSELOR | List referrals |
| GET | `/referrals/{id}` | ADMIN, COUNSELOR | Get referral |
| GET | `/referrals/client/{clientId}` | ADMIN, COUNSELOR | Referrals for a client |
| GET | `/referrals/case/{caseId}` | ADMIN, COUNSELOR | Referrals for a case |
| PATCH | `/referrals/{id}/status` | ADMIN, COUNSELOR | Update referral status |
| DELETE | `/referrals/{id}` | ADMIN, COUNSELOR | Delete referral |
| POST | `/visits` | ADMIN, COUNSELOR | Record a clinic visit manually |
| POST | `/visits/inbound` | **service-to-service** (`X-Service-Api-Key` header) | Inbound webhook from clinic system — **fixed in item 7**, was ADMIN/COUNSELOR JWT, now matches the security-alerts webhook pattern |
| GET | `/visits/client/{clientId}` | ADMIN, COUNSELOR | Visits for a client |
| GET | `/visits/client/{clientId}/frequency` | ADMIN, COUNSELOR | Visit frequency stats |
| GET | `/visits/frequent-visitors` | ADMIN, COUNSELOR | Frequent visitors list |
| POST | `/security-alerts/inbound` | **service-to-service** (`X-Service-Api-Key` header) | Inbound security alert from clinic system |
| PATCH | `/security-alerts/inbound/{id}/status` | **service-to-service** (`X-Service-Api-Key` header) | Status update for a locally-known alert |

`/clinic/visits/inbound` and `/clinic/security-alerts/inbound/**` are `permitAll` in `SecurityConfig` and instead protected by the `X-Service-Api-Key` header (checked against `app.cross-system.api-key`), never by user JWT.

### 16. ConsentFormController
Base: `/consent` — **fixed in item 4**, previously no authorization at all

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/forms` | ADMIN, COUNSELOR | Create consent form |
| PUT | `/forms/{id}` | ADMIN, COUNSELOR | Update consent form |
| GET | `/forms/{id}` | authenticated | Get a consent form |
| GET | `/forms` | ADMIN, COUNSELOR | List all consent forms |
| GET | `/forms/active` | authenticated | List active consent forms |
| GET | `/forms/latest` | authenticated | Latest active consent form |
| POST | `/sign` | authenticated | Sign a consent form (bound to caller's `Principal`) |
| GET | `/check-signed` | authenticated | Whether caller has signed the latest form |
| GET | `/history` | authenticated | Caller's own consent history |
| GET | `/statistics` | ADMIN, COUNSELOR | Consent statistics |
| DELETE | `/forms/{id}` | ADMIN, COUNSELOR | Delete consent form |
| POST | `/forms/{id}/activate` | ADMIN, COUNSELOR | Activate consent form |
| POST | `/forms/{id}/deactivate` | ADMIN, COUNSELOR | Deactivate consent form |

### 17. ContactsController
Base: `/contacts`

| Method | Path | Purpose |
|---|---|---|
| GET | `/available` | Available contacts for messaging |
| GET | `/search` | Search contacts |

### 18. ConversationsController
Base: `/conversations`

| Method | Path | Purpose |
|---|---|---|
| GET | `/user/{userId}` | Conversations for a user |
| GET | `/` | Current user's conversations |
| GET | `/{partnerId}` | Conversation thread with a partner |
| PUT | `/{partnerId}/read` | Mark conversation as read |

### 19. CounselorController
Base: `/counselors`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/` | authenticated | List all counselors |
| GET | `/{id}` | authenticated | Get counselor by id |
| POST | `/` | ADMIN | Create counselor |
| DELETE | `/{id}` | ADMIN | Delete counselor |

Note: there is **no** `/counselors/available` or `/counselors/{id}/availability` route — the `SecurityConfig` `permitAll` rules that referenced these were dead config and were removed as part of fix #6.

### 20. CrisisAlertController
Base: `/crisis-alerts`, class-level `@PreAuthorize("hasAnyRole('COUNSELOR','ADMIN','SUPER_ADMIN')")` — **fixed in item 1**, previously used `ROLE_`-prefixed role names and matched nobody

| Method | Path | Purpose |
|---|---|---|
| GET | `/` | List alerts (paginated, optional status filter) |
| GET | `/pending-count` | Count of pending alerts |
| GET | `/{id}` | Get alert by id |
| PATCH | `/{id}/acknowledge` | Acknowledge alert |
| PATCH | `/{id}/resolve` | Resolve alert |
| PATCH | `/{id}/false-positive` | Mark as false positive |

### 21. DashboardConfigController
Base: `/dashboard`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/config` | authenticated | **Own** dashboard widget config — fixed in item 5 |
| POST | `/config` | authenticated | Save **own** dashboard widget config |
| DELETE | `/config` | authenticated | Reset **own** dashboard to default |

### 22. DashboardController
Base: `/dashboard`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/stats` | authenticated | Dashboard stats |
| GET | `/analytics` | ADMIN, COUNSELOR | Analytics overview |
| GET | `/high-risk-clients` | ADMIN, COUNSELOR | High-risk client list |
| GET | `/recent-clients` | ADMIN, COUNSELOR | Recently added clients |
| GET | `/recent-activity` | ADMIN, COUNSELOR | Recent activity feed |
| GET | `/performance-metrics` | ADMIN, COUNSELOR | Performance metrics |
| GET | `/upcoming-appointments` | ADMIN, COUNSELOR | Upcoming appointments |
| GET | `/at-risk-students` | ADMIN, COUNSELOR | At-risk students |

### 23. FeedbackController
Base: `/feedback`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/` | authenticated | Submit feedback (owner = caller) |
| GET | `/my` | authenticated | **Own** feedback — fixed in item 5 (was `?userId=` IDOR) |
| GET | `/user` | authenticated | Current user's feedback (already principal-bound) |
| GET | `/` | ADMIN | List all feedback |
| GET | `/status/{status}` | ADMIN | Feedback by status |
| GET | `/category/{category}` | ADMIN | Feedback by category |
| GET | `/{id}` | ADMIN, COUNSELOR, STUDENT, CLIENT | Get feedback by id |
| GET | `/stats` | ADMIN | Feedback stats |
| PUT | `/{id}` | ADMIN, COUNSELOR, STUDENT, CLIENT | Update feedback |
| DELETE | `/{id}` | ADMIN, COUNSELOR, STUDENT, CLIENT | Delete feedback |
| PUT | `/{id}/status` | ADMIN | Update feedback status |

### 24. GoalController
Base: `/goals`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/` | ADMIN, COUNSELOR | List all goals |
| GET | `/{id}` | ADMIN, COUNSELOR, CLIENT | Get goal by id |
| GET | `/client/{clientId}` | ADMIN, COUNSELOR, CLIENT | Goals for a client |
| GET | `/client/{clientId}/paginated` | ADMIN, COUNSELOR, CLIENT | Paginated goals for a client |
| GET | `/client/{clientId}/status/{status}` | ADMIN, COUNSELOR, CLIENT | Goals by status |
| GET | `/client/{clientId}/category/{category}` | ADMIN, COUNSELOR, CLIENT | Goals by category |
| POST | `/` | ADMIN, COUNSELOR, CLIENT | Create goal |
| PUT | `/{id}` | ADMIN, COUNSELOR, CLIENT | Update goal |
| DELETE | `/{id}` | ADMIN | Delete goal |
| PUT | `/{id}/progress` | ADMIN, COUNSELOR, CLIENT | Update progress |
| PUT | `/{id}/status` | ADMIN, COUNSELOR, CLIENT | Update status |
| GET | `/client/{clientId}/stats` | ADMIN, COUNSELOR, CLIENT | Goal stats for a client |
| GET | `/overdue` | ADMIN, COUNSELOR | Overdue goals |
| GET | `/search` | ADMIN, COUNSELOR | Search goals |

### 25. HealthController
Base: `/health` — all public

| Method | Path | Purpose |
|---|---|---|
| GET | `/` | Health check |
| GET | `/ready` | Readiness probe |
| GET | `/live` | Liveness probe |
| GET | `/system` | System health details |

Plus Spring Boot Actuator endpoints under `/actuator/**` (public per `SecurityConfig`).

### 26. KeyboardShortcutController
Base: `/keyboard-shortcuts`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/` | authenticated | **Own** shortcuts — fixed in item 5 |
| GET | `/enabled` | authenticated | Own enabled shortcuts |
| POST | `/` | authenticated | Save/update a shortcut (owner = caller) |
| PUT | `/{id}` | authenticated, owner-checked | Update a shortcut |
| DELETE | `/{id}` | authenticated, owner-checked | Delete a shortcut |
| POST | `/reset` | authenticated | Reset own shortcuts to defaults |

### 27. KnowledgeBaseController
Base: `/knowledge-base`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/` | public | Published articles |
| GET | `/{id}` | public | Get article |
| GET | `/search` | public | Search articles |
| GET | `/category/{category}` | public | Articles by category |
| POST | `/` | ADMIN, COUNSELOR | Create article |
| PUT | `/{id}` | ADMIN, COUNSELOR | Update article |
| DELETE | `/{id}` | ADMIN | Delete article |

### 28. MentalHealthAcademicAnalysisController
Base: `/analysis`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/high-risk` | ADMIN, COUNSELOR | High-risk analyses |
| GET | `/urgent` | ADMIN, COUNSELOR | Urgent interventions |
| GET | `/client/{clientId}` | ADMIN, COUNSELOR, CLIENT, STUDENT | Analyses for a client |
| GET | `/client/{clientId}/latest` | ADMIN, COUNSELOR, CLIENT | Latest analysis for a client |
| GET | `/client/{clientId}/trend` | ADMIN, COUNSELOR | Trend data |
| GET | `/dashboard-stats` | ADMIN, COUNSELOR | Dashboard stats |
| GET | `/intervention-report` | ADMIN, COUNSELOR | Intervention report |
| POST | `/` | ADMIN, COUNSELOR | Create analysis |
| PUT | `/{id}` | ADMIN, COUNSELOR | Update analysis |
| GET | `/{id}` | ADMIN, COUNSELOR, CLIENT | Get analysis by id |
| DELETE | `/{id}` | ADMIN | Delete analysis — **fixed in item 8**, previously did not actually delete |

### 29. MessageController
Base: `/messages`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/send` | authenticated | Send message |
| POST | `/` | authenticated | Send message (v2) |
| GET | `/` | authenticated | List caller's messages |
| GET | `/{id}` | authenticated | Get message by id |
| PUT | `/{id}` | authenticated | Update message |
| DELETE | `/{id}` | authenticated | Delete message |
| GET | `/conversation/{conversationId}` | authenticated | Messages in a conversation |
| GET | `/search` | authenticated | Search messages |
| PUT | `/{messageId}/read` | authenticated | Mark as read |
| PUT | `/{messageId}/delivered` | authenticated | Mark as delivered |
| PUT | `/read-all` | authenticated | Mark all as read |
| GET | `/unread-count` | authenticated | Unread count |
| GET | `/statistics` | authenticated | Message statistics |
| POST | `/{messageId}/reply` | authenticated | Reply to a message |
| POST | `/{messageId}/forward` | authenticated | Forward a message |
| PUT | `/{id}/archive` | authenticated | Archive message |
| PUT | `/{id}/unarchive` | authenticated | Unarchive message |
| GET | `/archived` | authenticated | List archived messages |
| PUT | `/{id}/star` | authenticated | Star message |
| PUT | `/{id}/unstar` | authenticated | Unstar message |
| GET | `/starred` | authenticated | List starred messages |
| POST | `/bulk-delete` | authenticated | Bulk delete |
| POST | `/bulk-read` | authenticated | Bulk mark as read |
| GET | `/audit` | ADMIN | Message audit records |
| GET | `/audit/stats` | ADMIN | Message audit stats |

### 30. NotificationController
Base: `/notifications`

| Method | Path | Purpose |
|---|---|---|
| GET | `/user/{userId}` | Notifications for a user |
| GET | `/user/{userId}/notifications` | Alias of above |
| GET | `/` | Current user's notifications |
| PUT | `/{id}/read` | Mark as read |
| PUT | `/read-all`, `/user/{userId}/mark-all-read` | Mark all as read |
| GET | `/unread-count`, `/user/{userId}/unread-count` | Unread count |
| DELETE | `/{id}` | Delete notification |

### 31. PersonalDataFormController
Base: `/personal-data-forms`, class-level `@PreAuthorize("hasAnyRole('ADMIN','COUNSELOR')")` — **fixed in item 3**, previously unrestricted

| Method | Path | Purpose |
|---|---|---|
| POST | `/clients/{clientId}` | Create personal data form for a client |
| GET | `/clients/{clientId}` | Get by client id |
| GET | `/file/{clientFileNo}` | Get by client file number |
| GET | `/cases/{caseId}` | Get by case id |
| PUT | `/clients/{clientId}` | Update |
| DELETE | `/clients/{clientId}` | Delete |

### 32. ReportController
Base: `/reports`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/` | ADMIN, COUNSELOR | List reports |
| GET | `/{id}` | ADMIN, COUNSELOR | Get report |
| POST | `/` | ADMIN, COUNSELOR | Generate report |
| POST | `/counselor` | ADMIN, COUNSELOR | Create counselor report |
| PUT | `/counselor/{id}` | ADMIN, COUNSELOR | Update counselor report |
| PUT | `/{id}` | ADMIN | Update report |
| DELETE | `/{id}` | ADMIN | Delete report |
| GET | `/types` | ADMIN, COUNSELOR | Report types |
| POST | `/schedule` | ADMIN | Schedule a report |
| GET | `/export/{id}` | ADMIN, COUNSELOR | Export report |
| GET | `/history` | ADMIN, COUNSELOR | Report history |
| GET | `/scheduled` | ADMIN, COUNSELOR | Scheduled reports |
| PUT | `/schedule/{scheduleId}` | ADMIN | Update schedule |
| DELETE | `/schedule/{scheduleId}` | ADMIN | Delete schedule |
| GET | `/statistics` | ADMIN, COUNSELOR | Report statistics |
| GET | `/analytics` | ADMIN, COUNSELOR | Report analytics |
| POST | `/{id}/duplicate` | ADMIN, COUNSELOR | Duplicate report |
| POST | `/{id}/archive` | ADMIN | Archive report |
| POST | `/{id}/restore` | ADMIN | Restore report |
| GET | `/archived` | ADMIN, COUNSELOR | Archived reports |
| GET | `/summary` | ADMIN, COUNSELOR | Report summary |
| GET | `/appointment-trends` | ADMIN, COUNSELOR | Appointment trends |
| GET | `/presenting-concerns` | ADMIN, COUNSELOR | Presenting concerns breakdown |
| GET | `/recent-sessions` | ADMIN, COUNSELOR | Recent sessions |
| GET | `/all` | ADMIN, COUNSELOR | All report data |
| GET | `/export` | ADMIN | Legacy export |

### 33. ResourceController
Base: `/resources`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/` | public | List resources |
| GET | `/{id}` | public | Get resource |
| POST | `/` (JSON) | ADMIN, COUNSELOR | Create resource |
| PUT | `/{id}` | ADMIN, COUNSELOR | Update resource |
| POST | `/` (multipart) | ADMIN, COUNSELOR | Create resource with file |
| POST | `/upload` | ADMIN, COUNSELOR | Upload resource file |
| DELETE | `/{id}` | ADMIN, COUNSELOR | Delete resource |
| GET | `/search` | public | Search resources |
| GET | `/categories` | public | List categories |
| GET | `/stats` | public | Resource stats |
| GET | `/type/{type}` | public | Resources by type |
| GET | `/category/{category}` | public | Resources by category |
| GET | `/featured` | public | Featured resources |
| GET | `/export` | public | Export resources |
| GET | `/download/{id}` | public | Download resource file |

### 34. RiskAssessmentController
Base: `/risk-assessments`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/` | ADMIN, COUNSELOR | List risk assessments |
| GET | `/{id}` | ADMIN, COUNSELOR | Get by id |
| POST | `/` | ADMIN, COUNSELOR | Create risk assessment |
| PUT | `/{id}` | ADMIN, COUNSELOR | Update |
| DELETE | `/{id}` | ADMIN | Delete |
| GET | `/client/{clientId}` | ADMIN, COUNSELOR | Assessments for a client |
| GET | `/high-risk` | ADMIN, COUNSELOR | High-risk assessments |
| GET | `/stats` | ADMIN, COUNSELOR | Stats |
| POST | `/{id}/escalate` | ADMIN, COUNSELOR | Escalate assessment |
| GET | `/export` | ADMIN | Export |
| GET | `/client/{clientId}/latest` | ADMIN, COUNSELOR, CLIENT | Latest for a client |
| GET | `/client/{clientId}/trend` | ADMIN, COUNSELOR | Trend for a client |
| GET | `/summary` | ADMIN, COUNSELOR | Summary |
| GET | `/follow-up-required` | ADMIN, COUNSELOR | Assessments needing follow-up |
| GET | `/assessor` | ADMIN, COUNSELOR | Assessments by assessor |
| GET | `/analytics` | ADMIN, COUNSELOR | Analytics |

### 35. ScholarshipController
Base: `/scholarships`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/` | ADMIN, COUNSELOR | Create scholarship |
| GET | `/{id}` | ADMIN, COUNSELOR | Get scholarship |
| GET | `/` | ADMIN, COUNSELOR | List scholarships |
| GET | `/active` | ADMIN, COUNSELOR | Active scholarships |
| PUT | `/{id}` | ADMIN, COUNSELOR | Update scholarship |
| DELETE | `/{id}` | ADMIN | Delete scholarship |
| GET | `/{id}/eligible-students` | ADMIN, COUNSELOR | Eligible students |
| POST | `/recommendations` | ADMIN, COUNSELOR | Create recommendation |
| GET | `/recommendations` | ADMIN, COUNSELOR | List recommendations |
| GET | `/recommendations/{id}` | ADMIN, COUNSELOR | Get recommendation |
| GET | `/{scholarshipId}/recommendations` | ADMIN, COUNSELOR | Recommendations for a scholarship |
| GET | `/recommendations/client/{clientId}` | ADMIN, COUNSELOR | Recommendations for a client |
| GET | `/recommendations/status/{status}` | ADMIN | Recommendations by status |
| PUT | `/recommendations/{id}` | ADMIN, COUNSELOR | Update recommendation |
| PATCH | `/recommendations/{id}/status` | ADMIN | Change recommendation status |
| DELETE | `/recommendations/{id}` | ADMIN, COUNSELOR | Delete recommendation |

### 36. SecurityAlertController
Base: `/security-alerts`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/` | ADMIN, COUNSELOR, SUPER_ADMIN | Create security alert (staff-raised) |
| POST | `/panic` | any authenticated user | Panic-button alert |
| GET | `/` | ADMIN, SUPER_ADMIN, SECURITY | List security alerts |
| POST | `/{id}/acknowledge` | ADMIN, SUPER_ADMIN, SECURITY | Acknowledge alert |
| POST | `/{id}/resolve` | ADMIN, SUPER_ADMIN, SECURITY | Resolve alert |

### 37. SelfAssessmentController
Base: `/self-assessments`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/` | STUDENT, CLIENT, COUNSELOR, ADMIN | List all assessments |
| POST | `/` | ADMIN, COUNSELOR | Create assessment (staff-entered) |
| POST | `/submit` | STUDENT, CLIENT | Submit own self-assessment |
| GET | `/client` | STUDENT, CLIENT, COUNSELOR, ADMIN | Caller's own assessments |
| GET | `/client/{clientId}/latest` | STUDENT, CLIENT, COUNSELOR, ADMIN | Latest for a client |
| GET | `/client/{clientId}/trend` | STUDENT, CLIENT, COUNSELOR, ADMIN | Trend for a client |

### 38. SessionController
Base: `/sessions`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/` | ADMIN, COUNSELOR | List sessions |
| GET | `/{id}` | ADMIN, COUNSELOR | Get session |
| POST | `/` | ADMIN, COUNSELOR | Create session |
| PUT | `/{id}` | ADMIN, COUNSELOR | Update session |
| DELETE | `/{id}` | ADMIN | Delete session |
| GET | `/client/{clientId}` | ADMIN, COUNSELOR, CLIENT | Sessions for a client |

### 39. SessionNoteController
Base: `/sessions` (shares the base path with `SessionController`, sub-routed under `/notes`)

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/notes` | ADMIN, COUNSELOR | List all session notes |
| GET | `/{sessionId}/notes` | ADMIN, COUNSELOR | Notes for a session |
| POST | `/notes` | ADMIN, COUNSELOR | Create note |
| PUT | `/notes/{noteId}` | ADMIN, COUNSELOR | Update note |

### 40. SettingsController
Base: `/settings`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/` | ADMIN, COUNSELOR | All settings |
| GET | `/organization` | ADMIN, COUNSELOR | Organization settings |
| PUT | `/organization` | ADMIN | Update organization settings |
| GET | `/appointments` | ADMIN, COUNSELOR | Appointment settings |
| PUT | `/appointments` | ADMIN | Update appointment settings |
| GET | `/notifications` | ADMIN, COUNSELOR | Notification settings |
| PUT | `/notifications` | ADMIN | Update notification settings |
| GET | `/security` | ADMIN | Security settings |
| PUT | `/security` | ADMIN | Update security settings |
| GET | `/{category}` | ADMIN, COUNSELOR | Settings by category |
| PUT | `/{key}` | ADMIN, COUNSELOR | Update a setting |
| POST | `/` | ADMIN, COUNSELOR | Create a setting |
| DELETE | `/{key}` | ADMIN, COUNSELOR | Delete a setting |
| GET | `/health` | public | Settings module health check |
| PUT | `/` | ADMIN | Update all settings |
| PUT | `/{category}/{key}` | ADMIN | Update setting by key |
| PUT | `/security/{key}` | ADMIN | Update security setting |
| PUT | `/organization/{key}` | ADMIN | Update organization setting |
| PUT | `/notifications/{key}` | ADMIN | Update notification setting |
| PUT | `/appointments/{key}` | ADMIN | Update appointment setting |
| GET | `/appearance` | ADMIN, COUNSELOR | Appearance/theme settings |
| PUT | `/appearance` | ADMIN, COUNSELOR | Update appearance settings |
| PUT | `/appearance/{key}` | ADMIN, COUNSELOR | Update one appearance setting |

### 41. UserController
Base: `/users`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/` | (no explicit `@PreAuthorize` — falls back to global `anyRequest().authenticated()`) | List all users |
| GET | `/{userId}/notification-settings` | ADMIN, COUNSELOR, STUDENT, CLIENT | Get notification settings |
| PUT | `/{userId}/notification-settings` | ADMIN, COUNSELOR, STUDENT, CLIENT | Update notification settings |
| GET | `/{id}/is-anonymous` | ADMIN, COUNSELOR, STUDENT, CLIENT | Whether a user is an anonymous-session account |
| GET | `/{id}` | ADMIN, COUNSELOR | Get user by id |
| GET | `/email/{email}` | ADMIN, COUNSELOR | Get user by email |
| POST | `/` | ADMIN | Create user |
| PUT | `/{id}` | ADMIN | Update user |
| DELETE | `/{id}` | ADMIN | Soft-delete user |
| DELETE | `/{id}/permanent` | ADMIN | Hard-delete user |
| GET | `/role/{role}` | ADMIN, COUNSELOR | Users by role |
| GET | `/count` | ADMIN, COUNSELOR | User count |
| GET | `/search` | ADMIN, COUNSELOR | Search users |
| GET | `/active` | ADMIN, COUNSELOR | Active users |
| GET | `/inactive` | ADMIN, COUNSELOR | Inactive users |
| PUT | `/{id}/activate` | ADMIN | Activate user |
| PUT | `/{id}/deactivate` | ADMIN | Deactivate user |
| GET | `/roles` | ADMIN, COUNSELOR | List all roles |
| GET | `/count-by-role` | ADMIN, COUNSELOR | User count by role |
| GET | `/profile` | ADMIN, COUNSELOR, STUDENT, CLIENT | Current user's profile (principal-bound reference pattern used throughout this update) |
| PUT | `/profile` | ADMIN, COUNSELOR, STUDENT, CLIENT | Update current user's profile |
| PUT | `/{id}/password` | ADMIN | Admin-set a user's password |
| GET | `/export` | ADMIN | Export users |

Note: `GET /users` (list all) has no `@PreAuthorize` of its own; it still requires authentication via the global `anyRequest().authenticated()` rule in `SecurityConfig` but is not further role-restricted at the method level. Flagged here for awareness — not changed as part of this pass since it was outside the audited scope (items 1–8 above), but worth a follow-up review given it returns the full `User` list to any authenticated role.

### 42. WebSocketEventController
`@Controller` (STOMP `@MessageMapping`, not a `@RestController`). Reached over the `/api/ws` WebSocket connection; auth happens at the STOMP `CONNECT` frame (JWT), not via HTTP `@PreAuthorize`.

| Destination | Purpose |
|---|---|
| `/app/message.send/{recipientId}` | Send a chat message |
| `/app/typing.start/{partnerId}` | Typing indicator start |
| `/app/typing.stop/{partnerId}` | Typing indicator stop |
| `/app/message.delivered/{messageId}` | Mark message delivered |
| `/app/status.online` | Broadcast online status |
| `/app/status.offline` | Broadcast offline status |

Clients subscribe to `/topic/**` and `/queue/**` destinations for broadcast/point-to-point delivery.

---

## SecurityConfig public (`permitAll`) routes, for reference

- `/auth/**` (and `/api/auth/**`, `/v1/auth/**`, `/api/v1/auth/**`)
- `/appointments/stats`, `/appointments/availability` (and `/api/...` variants) — see fix #6
- `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`, `/api-docs/**`
- `/actuator/**`, `/health`
- `/ws/**`, `/api/ws/**` (WebSocket handshake — STOMP `CONNECT` frame carries the JWT)
- `/clinic/security-alerts/inbound/**`, `/clinic/visits/inbound` (and `/api/...` variants) — service-to-service, `X-Service-Api-Key` gated, see fix #7
- OPTIONS `/**` (CORS preflight)

Everything else requires a valid JWT (`anyRequest().authenticated()`), further narrowed per-endpoint by the `@PreAuthorize` annotations documented above.
