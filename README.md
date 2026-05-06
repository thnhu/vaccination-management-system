# Vaccination Management System - National Vaccination MVP

A robust Java Spring Boot backend system for managing national vaccination records, appointments, vaccine batches, and citizen profiles.

## Tech Stack

- **Java**: 21
- **Framework**: Spring Boot 4.0.6
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA + Hibernate
- **Database**: SQL Server
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
   Update `src/main/resources/application.properties` with your SQL Server connection details.

3. **Build and run**
   ```bash
   mvn spring-boot:run
   ```


## API Base Path

All endpoints are prefixed with `/vaccination`

### Main Modules

**Authentication**
- `POST /auth/register`
- `POST /auth/login`

**Vaccine**
- `POST /vaccines`
- `GET /vaccines`
- `GET /vaccines/{id}`

**Facility**
- `POST /facilities`
- `GET /facilities`

**Vaccine Batch**
- `POST /facilities/{id}/batches`

**Appointment**
- `POST /appointments` (Citizen)
- `GET /appointments/my` (Citizen)
- `GET /appointments/today` (Medical staff)

**Vaccination Record**
- `POST /vaccination-records` (Medical staff)
- `POST /vaccination-records/retrospective` (Medical staff)
- `GET /citizens/me` (Citizen)

## Important Design Highlights

- Vaccination records are **immutable** (no UPDATE or DELETE) to ensure legal compliance and full audit trail.
- Optimistic Locking on Facility to prevent overbooking of slots.
- Guard Clause pattern for clean and maintainable business logic.
- Thorough input validation + business rule validation at Service layer.

## Development Workflow

- Branch strategy: `main` → feature branches
- Conventional Commits
- Clear separation of concerns (Controller → Service → Repository)
