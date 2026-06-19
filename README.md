# Vaccination Management System

A Java Spring Boot backend for managing vaccination records, appointments, vaccine batches, and citizen profiles — modeled after private vaccination chains (VNVC-style).

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Security | Spring Security + JWT |
| ORM | Spring Data JPA + Hibernate |
| Database | SQL Server |
| AI | Anthropic Claude API (Citizen Advisor) |
| Build Tool | Maven |
## Key Features

**Core Domain**
- Role-based authentication: `CITIZEN`, `MEDICAL_STAFF`, `FACILITY_ADMIN`
- Vaccine catalog with multi-dose schedule support (variable intervals between doses)
- Disease catalog with ICD-10 codes, linked to vaccines via many-to-many
- Manufacturer & Supplier management with full supply chain traceability
- Facility management with versioned daily capacity (Optimistic Locking on `FacilityCapacity`)
- Vaccine batch tracking: inventory control, batch recall workflow
- Appointment booking with automatic dose interval validation and real-time slot availability
- Appointment status history audit trail (every status change logged)
- Vaccination record management: direct recording, retrospective entry, invalidation
- Vaccination reaction tracking per record (MILD / MODERATE / SEVERE), with report source distinction (staff-observed vs. citizen self-report)
- Immutable vaccination records for legal compliance — correction creates a replacement record, never overwrites
- Administrative unit hierarchy: 2 levels (PROVINCE / WARD) per Vietnam's 2026 reform

**AI Advisor**
- Citizen-facing conversational advisor powered by Anthropic Claude
- Agentic loop: Claude calls internal tools (`get_vaccination_history`, `get_recommended_schedule`, `get_available_slots`) via Anthropic Tool Use
- Provider-agnostic `LlmClient` interface — swap provider via a single config line
- Multi-turn conversation with in-memory session history and windowing (last 6 messages)
- Prompt caching on system prompt + tool definitions to reduce token cost
- FAQ short-circuit: 15 common vaccination questions answered from cache without calling the LLM
- 2-layer guardrail: system prompt constraints + keyword filter post-processing

## Project Structure

```
com.yourname.vaccination/
│
├── controller/
├── service/
├── repository/
│
├── model/
│   ├── entity/
│   │   ├── User.java
│   │   ├── CitizenProfile.java
│   │   ├── MedicalStaffProfile.java
│   │   ├── AdministrativeUnit.java
│   │   ├── Vaccine.java
│   │   ├── VaccineDoseSchedule.java
│   │   ├── Disease.java
│   │   ├── VaccineDisease.java
│   │   ├── Manufacturer.java
│   │   ├── Supplier.java
│   │   ├── Facility.java
│   │   ├── FacilityCapacity.java
│   │   ├── VaccineBatch.java
│   │   ├── Appointment.java
│   │   ├── AppointmentStatusHistory.java
│   │   ├── VaccinationRecord.java
│   │   └── VaccinationReaction.java
│   ├── enums/
│   │   ├── UserRole              -- CITIZEN | MEDICAL_STAFF | FACILITY_ADMIN
│   │   ├── VaccineCategory       -- CHILD | ADULT | PREGNANT_WOMEN | ELDERLY | ALL_AGES
│   │   ├── Gender                -- MALE | FEMALE | OTHER
│   │   ├── FacilityType          -- VACCINATION_CENTER
│   │   ├── BatchStatus           -- ACTIVE | DEPLETED | RECALLED | EXPIRED
│   │   ├── AppointmentStatus     -- SCHEDULED | VACCINATED | CANCELLED | NO_SHOW
│   │   ├── VaccinationRecordStatus -- VALID | INVALID
│   │   ├── DataSource            -- SYSTEM | PAPER_BOOK | LAB_RESULT | SELF_REPORT
│   │   └── ReactionLevel         -- MILD | MODERATE | SEVERE
│   └── dto/
│       ├── user/ · vaccine/ · disease/ · manufacturer/ · supplier/
│       ├── facility/ · facilitycapacity/ · vaccinebatch/
│       ├── appointment/ · vaccinationrecord/
│       └── advisor/              -- AdvisorChatRequest/Response
│
├── advisor/
│   ├── controller/AdvisorController.java
│   ├── service/
│   │   ├── CitizenAdvisorService.java
│   │   ├── ToolExecutorService.java
│   │   ├── GuardrailService.java
│   │   └── FaqService.java
│   └── llm/
│       ├── LlmClient.java        -- provider-agnostic interface
│       └── AnthropicLlmClientImpl.java
│
├── exception/
│   ├── ResourceNotFoundException  -- → 404
│   ├── BusinessException          -- → 400
│   ├── DuplicateResourceException -- → 409
│   └── GlobalExceptionHandler.java
│
├── security/
├── common/
│   ├── ApiResponse.java
│   └── AppConstants.java
└── VaccinationApplication.java
```

## Prerequisites

- Java 21
- Maven 3.8+
- SQL Server
- Anthropic API key (for AI Advisor feature)

## Quick Start

1. **Clone the project**
   ```bash
   git clone <your-repository-url>
   cd vaccination-management-system
   ```

2. **Configure database and API key**

   Update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=vaccination_db
   spring.datasource.username=<your-username>
   spring.datasource.password=<your-password>

   anthropic.api-key=${ANTHROPIC_API_KEY}
   advisor.llm.provider=anthropic
   advisor.history.window-size=6
   ```

3. **Build and run**
   ```bash
   mvn spring-boot:run
   ```

4. **Run tests**
   ```bash
   mvn test
   ```

## API Reference

All endpoints are prefixed with `/vaccination`

**Auth:** `Authorization: Bearer <jwt_token>`  
**Response format:** `{ "success", "message", "data", "timestamp" }`

### Authentication

| Method | Endpoint | Auth | Notes |
|---|---|---|---|
| POST | `/auth/register` | Public | |
| POST | `/auth/login` | Public | Returns JWT + role |
| POST | `/auth/logout` | Public | |

### Vaccine

| Method | Endpoint | Auth | Role |
|---|---|---|---|
| POST | `/vaccines` | Required | FACILITY_ADMIN |
| GET | `/vaccines` | Public | — |
| GET | `/vaccines/{id}` | Public | — |
| PATCH | `/vaccines/{id}` | Required | FACILITY_ADMIN |
| PATCH | `/vaccines/{id}/deactivate` | Required | FACILITY_ADMIN |
| POST | `/vaccines/{id}/dose-schedules` | Required | FACILITY_ADMIN |

### Disease

| Method | Endpoint | Auth | Role |
|---|---|---|---|
| POST | `/diseases` | Required | FACILITY_ADMIN |
| GET | `/diseases` | Public | — |
| GET | `/diseases/{id}` | Public | — |
| PATCH | `/diseases/{id}` | Required | FACILITY_ADMIN |
| PATCH | `/diseases/{id}/deactivate` | Required | FACILITY_ADMIN |

### Manufacturer & Supplier

| Method | Endpoint | Auth | Role |
|---|---|---|---|
| POST | `/manufacturers` | Required | FACILITY_ADMIN |
| GET | `/manufacturers` | Required | MEDICAL_STAFF / FACILITY_ADMIN |
| GET | `/manufacturers/{id}` | Required | MEDICAL_STAFF / FACILITY_ADMIN |
| PATCH | `/manufacturers/{id}` | Required | FACILITY_ADMIN |
| POST | `/suppliers` | Required | FACILITY_ADMIN |
| GET | `/suppliers` | Required | MEDICAL_STAFF / FACILITY_ADMIN |
| GET | `/suppliers/{id}` | Required | MEDICAL_STAFF / FACILITY_ADMIN |
| PATCH | `/suppliers/{id}` | Required | FACILITY_ADMIN |

### Facility

| Method | Endpoint | Auth | Role |
|---|---|---|---|
| POST | `/facilities` | Required | FACILITY_ADMIN |
| GET | `/facilities` | Public | — |
| GET | `/facilities/{id}` | Public | — |
| PATCH | `/facilities/{id}` | Required | FACILITY_ADMIN |
| PATCH | `/facilities/{id}/deactivate` | Required | FACILITY_ADMIN |
| POST | `/facilities/{id}/capacity` | Required | FACILITY_ADMIN |
| GET | `/facilities/{id}/capacity` | Required | FACILITY_ADMIN |

### Vaccine Batch

| Method | Endpoint | Auth | Role | Notes |
|---|---|---|---|---|
| POST | `/facilities/{id}/batches` | Required | FACILITY_ADMIN | Import new batch |
| GET | `/facilities/{id}/batches` | Required | MEDICAL_STAFF / FACILITY_ADMIN | List batches at facility |
| PATCH | `/batches/{id}/recall` | Required | FACILITY_ADMIN | Recall a batch |

### Appointment

| Method | Endpoint | Auth | Role | Notes |
|---|---|---|---|---|
| POST | `/appointments` | Required | CITIZEN | Book appointment |
| GET | `/appointments/my` | Required | CITIZEN | Own appointments |
| GET | `/appointments/today` | Required | MEDICAL_STAFF | Today's schedule |
| PATCH | `/appointments/{id}/cancel` | Required | CITIZEN | Reason required |
| PATCH | `/appointments/{id}/no-show` | Required | MEDICAL_STAFF | Mark no-show |
| GET | `/appointments/{id}/history` | Required | MEDICAL_STAFF / FACILITY_ADMIN | Status change log |

### Vaccination Record

| Method | Endpoint | Auth | Role | Notes |
|---|---|---|---|---|
| POST | `/vaccination-records` | Required | MEDICAL_STAFF | Record vaccination |
| POST | `/vaccination-records/retrospective` | Required | MEDICAL_STAFF | Retrospective entry |
| PATCH | `/vaccination-records/{id}/invalidate` | Required | MEDICAL_STAFF | Invalidate wrong record |
| GET | `/citizens/{id}/vaccination-records` | Required | CITIZEN (own) / MEDICAL_STAFF | Vaccination history |

### Citizen

| Method | Endpoint | Auth | Role |
|---|---|---|---|
| GET | `/citizens/me` | Required | CITIZEN |
| PATCH | `/citizens/me` | Required | CITIZEN |

### AI Advisor

| Method | Endpoint | Auth | Role | Notes |
|---|---|---|---|---|
| POST | `/advisor/chat` | Required | CITIZEN | Multi-turn chat |
| GET | `/advisor/recommendations` | Required | CITIZEN | Next dose schedule |
| GET | `/advisor/available-slots` | Public | — | Slot availability by facility/date |

**Example advisor request:**
```bash
curl -X POST http://localhost:8080/vaccination/advisor/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <citizen_jwt>" \
  -d '{"message": "Tôi cần tiêm gì tiếp theo?", "sessionId": null}'
```

**Example response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "response": "Dựa trên lịch sử tiêm của bạn, bạn cần tiêm mũi 2...",
    "sessionId": "uuid-xxxx"
  },
  "timestamp": "2026-06-19T10:00:00"
}
```

## Design Highlights

**Immutable vaccination records** — no UPDATE or DELETE on clinical data. Correction creates a new record with `replaces_record_id` pointing to the original; the original is set to `INVALID` in the same transaction.

**Optimistic Locking on `FacilityCapacity`** — `@Version` is placed on `FacilityCapacity`, not on `Facility`, so concurrent booking transactions don't contend on master facility data. Conflict returns HTTP 409.

**Vaccine dose schedules as first-class data** — `VaccineDoseSchedule` table replaces `requiredDoses`/`daysBetweenDoses` flat fields, enabling schedules with unequal intervals between doses (e.g., Engerix-B: 30 days between dose 1–2, 150 days between dose 2–3).

**3NF normalization** — `Manufacturer` and `Supplier` are separate entities, enabling independent supply chain traceability. `Disease` with ICD-10 codes is decoupled from `Vaccine`.

**Provider-agnostic AI layer** — `LlmClient` interface abstracts the LLM provider. Swap from Anthropic to any other provider by changing one config line; `CitizenAdvisorService` has zero imports from the Anthropic SDK.

**FAQ short-circuit** — 15 common vaccination questions are answered from an in-memory keyword cache before the agentic loop is invoked, eliminating unnecessary API calls for predictable queries.

**Guard Clause pattern** — every Service method validates exhaustively before the happy path: fail fast, no nested if-else.

**2-level administrative units** — PROVINCE and WARD only, reflecting Vietnam's administrative reform effective May 2026.

## Test Coverage

**257 tests · 0 failures**

### Service Layer

| Test Class | Tests | What is covered |
|---|---|---|
| `AppointmentServiceTest` | 23 | Book (first dose, second dose, interval validation), cancel, no-show, guard clauses |
| `FacilityServiceTest` | 27 | Create, update, deactivate, capacity management |
| `VaccineServiceTest` | 23 | Create, update, deactivate, dose schedule linkage |
| `VaccinationRecordServiceTest` | 18 | Direct record, retrospective entry, invalidation, immutability |
| `VaccineBatchServiceTest` | 15 | Import batch, recall, inventory depletion |
| `CitizenServiceTest` | 13 | Profile view, profile update |
| `AuthServiceTest` | 9 | Register, login, duplicate phone/email |

### AI Advisor

| Test Class | Tests | What is covered |
|---|---|---|
| `AdvisorServiceTest` | 22 | Happy path (TC-01–05), tool behavior (TC-06–09), guardrail (TC-10–14), multi-turn (TC-15–17), edge cases (TC-18–21), history windowing (TC-22) |
| `FaqServiceTest` | 30 | Keyword matching, Vietnamese normalization, all 15 FAQ entries |
| `GuardrailServiceTest` | 6 | Dangerous keyword detection, emergency advice injection |
| `ToolExecutorServiceTest` | 8 | Tool dispatch to correct service, error propagation |
| `RecommendationServiceTest` | 7 | Next dose calculation, series completion, interval check |
| `AvailableSlotServiceTest` | 5 | Slot counting against capacity, date range |

### Controller Layer (Spring MVC integration)

| Test Class | Tests | What is covered |
|---|---|---|
| `AuthControllerTest` | 12 | Register/login success and error responses |
| `VaccinationRecordControllerTest` | 12 | Record endpoints, HTTP status codes |
| `AppointmentControllerTest` | 11 | Booking, cancel, no-show, history endpoints |
| `FacilityControllerTest` | 8 | Facility CRUD endpoints |
| `VaccineControllerTest` | 7 | Vaccine CRUD endpoints |

```
mvn test
# Tests run: 257, Failures: 0, Errors: 0, Skipped: 1
# BUILD SUCCESS
```

### JaCoCo Coverage Report

**Overall (all packages)**

![JaCoCo Overview](https://raw.githubusercontent.com/thnhu/vaccination-management-backend/feat/ai-advisor/docs/jacoco-overview.png)

**Service layer**

![JaCoCo Service Layer](https://raw.githubusercontent.com/thnhu/vaccination-management-backend/feat/ai-advisor/docs/jacoco-service.png)

**AI Advisor layer**

![JaCoCo Advisor Layer](https://raw.githubusercontent.com/thnhu/vaccination-management-backend/feat/ai-advisor/docs/jacoco-advisor.png)

## Development Workflow

- Branch strategy: `main` → `feat/<feature-name>`
- Conventional Commits
- Strict layer separation: Controller → Service → Repository (no cross-layer imports)
- Run `mvn test` before every merge; all tests must pass
