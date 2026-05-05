
```markdown
# Vaccination Management System - National Vaccination MVP

A robust Java Spring Boot backend system for managing national vaccination records, appointments, vaccine batches, and citizen profiles.

## Tech Stack

- **Java**: 21
- **Framework**: Spring Boot 4.0.5
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA + Hibernate
- **Database**: SQL Server
- **API Documentation**: Springdoc OpenAPI (Swagger UI)
- **Build Tool**: Maven

## Key Features

- User registration & role-based authentication (CITIZEN, MEDICAL_STAFF, FACILITY_ADMIN)
- Vaccine and Facility management
- Vaccine batch tracking with inventory control
- Appointment booking with real-time slot availability and automatic dose interval validation
- Vaccination record management (direct recording, retrospective entry, invalidation)
- Optimistic locking to handle concurrent appointment booking safely
- Immutable vaccination records for legal audit trail
- Comprehensive business rule validation and exception handling

## Project Structure

```
com.yourname.vaccination/
├── controller/
├── service/
├── repository/
├── model/
│   ├── entity/
│   └── dto/
├── exception/
├── security/
├── common/
└── VaccinationApplication.java
```

## Prerequisites

- Java 21
- Maven 3.8+
- SQL Server

## Quick Start

1. **Clone the project**
   ```bash
   git clone <your-repository-url>
   cd vaccination-system
   ```

2. **Configure database**
   Update `src/main/resources/application.yml` (or `application.properties`) with your SQL Server connection details.

3. **Build and run**
   ```bash
   mvn spring-boot:run
   ```

4. **Access Swagger UI**
   Open your browser and go to:
   ```
   http://localhost:8080/swagger-ui.html
   ```

## API Base Path

All endpoints are prefixed with `/vaccination`

### Main Modules

**Authentication**
- `POST /vaccination/auth/register`
- `POST /vaccination/auth/login`

**Vaccine**
- `POST /vaccination/vaccines`
- `GET /vaccination/vaccines`
- `GET /vaccination/vaccines/{id}`

**Facility**
- `POST /vaccination/facilities`
- `GET /vaccination/facilities`

**Vaccine Batch**
- `POST /vaccination/facilities/{id}/batches`

**Appointment**
- `POST /vaccination/appointments` (Citizen)
- `GET /vaccination/appointments/my` (Citizen)
- `GET /vaccination/appointments/today` (Medical staff)

**Vaccination Record**
- `POST /vaccination/vaccination-records` (Medical staff)
- `POST /vaccination/vaccination-records/retrospective` (Medical staff)
- `GET /vaccination/citizens/me` (Citizen)

> Role-based authorization is strictly enforced using JWT.

## Important Design Highlights

- Vaccination records are **immutable** (no UPDATE or DELETE) to ensure legal compliance and full audit trail.
- Optimistic Locking on Facility to prevent overbooking of slots.
- Guard Clause pattern for clean and maintainable business logic.
- Thorough input validation + business rule validation at Service layer.

## Development Workflow

- Branch strategy: `main` → feature branches
- Conventional Commits
- Clear separation of concerns (Controller → Service → Repository)

---

**Ready for deployment** on Railway, Render, or any Spring Boot supported platform.

```
