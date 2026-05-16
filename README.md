# Attendance Management System

A production-ready attendance management system with biometric device integration and WhatsApp notifications.

## Tech Stack

**Backend:** Java 17 · Spring Boot 3.2 · Spring Security (JWT) · Spring Data JPA · MySQL 8 · Redis · WebSocket (STOMP) · Twilio WhatsApp API · Flyway · Maven

**Frontend:** React 18 · Vite · Zustand · TanStack Query · Axios · Tailwind CSS · Recharts · SockJS/STOMP · React Hook Form + Zod

**Infrastructure:** Docker · Docker Compose

---

## Quick Start

### 1. Copy environment variables

```bash
cp .env.example .env
```

Edit `.env` with your secrets (JWT secret, optional Twilio credentials).

### 2. Start with Docker Compose

```bash
docker-compose up -d
```

Wait ~30 seconds for MySQL to initialize and Flyway to run migrations.

### 3. Access the application

| Service   | URL                                          |
|-----------|----------------------------------------------|
| Frontend  | http://localhost:5173                        |
| API       | http://localhost:8080/api                    |
| Swagger   | http://localhost:8080/api/swagger-ui.html    |

---

## Default Credentials

| Role    | Email                   | Password  |
|---------|-------------------------|-----------|
| Admin   | admin@school.com        | Admin@123 |
| Teacher | teacher1@school.com     | Admin@123 |
| Teacher | teacher2@school.com     | Admin@123 |

---

## Simulating a Biometric Event

First register or use the seeded device. Get the device API key from the register endpoint.
Then simulate a scan:

```bash
curl -X POST http://localhost:8080/api/biometric/events \
  -H "Content-Type: application/json" \
  -H "X-Device-API-Key: YOUR_DEVICE_API_KEY" \
  -d '{
    "biometricId": "B-001",
    "timestamp": "2024-01-15T08:05:00"
  }'
```

The system will:
1. Return `200 OK` immediately with an `eventId`
2. Asynchronously process the event:
   - Deduplicate (5-min window)
   - Match student by biometric ID
   - Find the active session for the student's class
   - Determine PRESENT/LATE based on grace period
   - Save attendance record
   - Send WhatsApp notification (mocked if TWILIO_ENABLED=false)
   - Broadcast via WebSocket to dashboard

---

## Registering a New Device

```bash
curl -X POST http://localhost:8080/api/devices \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Library Device",
    "location": "School Library"
  }'
```

Response includes the raw API key — **save it immediately**, it is only shown once.

---

## Architecture

```
Biometric Device → POST /biometric/events
                     ↓ (sync: validate + store DeviceEvent)
                     ↓ (async: @Async processEventAsync)
                         → Dedup check
                         → Resolve student + session
                         → Save AttendanceRecord
                         → WhatsApp notification
                         → WebSocket broadcast → Dashboard
```

## Scheduled Jobs

| Job                  | Schedule        | Purpose                              |
|----------------------|-----------------|--------------------------------------|
| AbsenteeMarkingJob   | Every 5 min     | Mark absent after session ends + 15m |
| DailySummaryJob      | Daily at 7:00AM | Send summary to all admins           |
| DeviceHealthCheckJob | Every 10 min    | Mark offline devices                 |
| WhatsApp retry       | Every 60s       | Retry failed messages (max 3 times)  |

## Role-Based Access

| Page       | ADMIN | TEACHER | STUDENT/PARENT |
|------------|-------|---------|----------------|
| Dashboard  | ✓     | ✓       | ✓ (own only)   |
| Attendance | ✓     | ✓       | -              |
| Students   | ✓     | ✓       | -              |
| Classes    | ✓     | -       | -              |
| Sessions   | ✓     | ✓       | -              |
| Devices    | ✓     | -       | -              |
| Reports    | ✓     | ✓       | -              |
