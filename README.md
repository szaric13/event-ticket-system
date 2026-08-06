# 🎫 TicketVault – Distributed Event Ticket Booking System

A distributed event ticket booking platform built with **Java 21**, **Spring Boot**, **Redis**, **RabbitMQ**, **PostgreSQL**, and **Docker**. The system demonstrates modern backend engineering concepts including **microservices**, **distributed systems**, **event-driven communication**, **concurrency control**, and **CI/CD**.

---

# 🚀 Overview

TicketVault is designed to solve two common problems in online ticketing systems:

- Preventing **overbooking** during concurrent purchase attempts.
- Automatically releasing unpaid ticket reservations after a configurable expiration period.

The application consists of **three independent microservices**, each with its own PostgreSQL database and responsibility.

---

# 🏗️ Architecture

```
                    +------------------+
                    |      Client      |
                    +------------------+
                              |
                              |
                    REST API Requests
                              |
        ------------------------------------------
        |                    |                  |
        |                    |                  |
+----------------+   +----------------+   +----------------+
| Inventory      |   | Booking        |   | Notification   |
| Service        |   | Service        |   | Service        |
+----------------+   +----------------+   +----------------+
        |                    |                  |
   PostgreSQL          PostgreSQL         PostgreSQL
        |                    |
      Redis <----------------|
        |
   Lua Scripts
        |
    Ticket Inventory

Booking ---- RabbitMQ ----> Notification
```

---

# 🧩 Microservices

## 📦 Inventory Service

Responsible for managing events and ticket inventory.

### Responsibilities

- Create events
- Store event metadata
- Track available tickets
- Perform atomic ticket reservations
- Prevent overselling

### REST Endpoints

| Method | Endpoint | Description |
|----------|--------------------------|----------------|
| GET | `/events` | List all events |
| POST | `/events` | Create new event |
| POST | `/events/{id}/reserve` | Reserve tickets |

### Technologies

- Spring Boot
- PostgreSQL
- Redis
- Redis Lua Scripting

---

## 🎟️ Booking Service

Responsible for reservation lifecycle.

### Responsibilities

- Create booking
- Confirm booking
- Handle reservation expiration
- Communicate with Inventory
- Publish events to RabbitMQ

### Booking Flow

```
Client
   |
POST /bookings
   |
Booking Service
   |
REST
   |
Inventory Service
   |
Redis Lua Script
   |
Ticket Reserved
   |
Booking Created (PENDING)
   |
Redis TTL (15 min)
```

When the booking is confirmed:

```
Booking
   |
CONFIRMED
   |
RabbitMQ
   |
Notification Service
```

If the booking expires:

```
Redis TTL expires
       |
Keyspace Notification
       |
Booking Status -> EXPIRED
       |
Inventory Restores Tickets
       |
RabbitMQ Event
```

---

## 📩 Notification Service

Consumes booking events from RabbitMQ.

### Responsibilities

- Booking confirmation notifications
- Booking expiration notifications
- Store notification history

---

# ⚡ Redis

Redis is used for:

- Ticket inventory
- Reservation TTL
- Atomic reservation logic

## Why Redis?

- Extremely fast (in-memory)
- Atomic operations
- Prevents race conditions
- Supports expiration events

---

# 🔒 Preventing Overbooking

The application uses a **Redis Lua Script** to reserve tickets atomically.

Instead of:

```
Read tickets
↓

Check availability
↓

Decrease tickets
```

which is vulnerable to race conditions,

the entire operation executes inside Redis as one atomic command.

Result:

- No overselling
- No double booking
- Thread-safe reservation

---

# ⏳ Reservation Expiration

Every reservation is stored inside Redis with a **15-minute TTL**.

```
SETEX booking:15 900
```

If payment is not completed:

- Redis automatically expires the key
- Booking becomes EXPIRED
- Reserved tickets are returned
- Notification event is published

No scheduled jobs or polling are required.

---

# 📨 RabbitMQ

RabbitMQ provides asynchronous communication between services.

## Exchange

```
booking.exchange
```

## Routing Keys

```
booking.confirmed

booking.expired
```

Benefits:

- Loose coupling
- Better scalability
- Independent services
- Reliable messaging

---

# 🗄️ Database per Service

Each microservice owns its own PostgreSQL database.

```
Inventory DB

Booking DB

Notification DB
```

Advantages:

- Loose coupling
- Independent deployment
- Independent schema evolution
- Better scalability

---

# 🐳 Docker

The project is fully containerized.

## Containers

Infrastructure

- Redis
- RabbitMQ
- PostgreSQL (Inventory)
- PostgreSQL (Booking)
- PostgreSQL (Notification)

Applications

- Inventory Service
- Booking Service
- Notification Service

Run everything with:

```bash
docker compose up --build
```

---

# ⚙️ CI/CD

GitHub Actions automatically:

- Build the project
- Run tests
- Verify services
- Prepare deployment

Every push triggers the pipeline automatically.

---

# ☁️ Deployment

Services are deployed independently on **Render**.

Cloud infrastructure:

- PostgreSQL → Neon
- Redis → Upstash
- RabbitMQ → CloudAMQP

---

# 📄 API Documentation

Each service exposes its own Swagger UI.

Examples:

- Inventory API
- Booking API
- Notification API

Swagger allows interactive API testing directly from the browser.

---

# 🛠️ Tech Stack

## Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Validation

## Messaging

- RabbitMQ

## Caching

- Redis
- Redis Lua

## Database

- PostgreSQL

## Build

- Maven

## DevOps

- Docker
- Docker Compose
- GitHub Actions

## Documentation

- Swagger / OpenAPI

---

# ⭐ Key Features

- Distributed Microservice Architecture
- Atomic Ticket Reservation
- Redis Lua Scripts
- Event-Driven Communication
- Automatic Reservation Expiration
- RabbitMQ Messaging
- Independent Databases
- Dockerized Infrastructure
- CI/CD Pipeline
- Live Deployment
- Swagger Documentation

---

# 📚 Concepts Demonstrated

- Distributed Systems
- Microservices
- REST APIs
- Event-Driven Architecture
- Concurrency Control
- Atomic Operations
- Asynchronous Messaging
- Database per Service Pattern
- Containerization
- Continuous Integration
- Continuous Deployment

---

# 💡 Future Improvements

- API Gateway
- Circuit Breaker (Resilience4j)
- Distributed Tracing
- Prometheus & Grafana Monitoring
- Kubernetes Deployment
- Rate Limiting
- OAuth2 Authentication
- Payment Gateway Integration

---

# 👨‍💻 Author

**Strahinja Zarić**

Full Stack Developer

Java • Spring Boot • Distributed Systems • React
