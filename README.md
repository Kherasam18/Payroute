<div align="center">

# 🔀 PayRoute

**Intelligent Payment Orchestration Engine**

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.4-brightgreen?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red?style=flat-square&logo=redis)](https://redis.io/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.x-231F20?style=flat-square&logo=apachekafka)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)](https://docs.docker.com/compose/)

</div>

---

A distributed payment orchestration engine that intelligently routes transactions across 5+ payment providers with automatic failover, strategy-driven routing, Redis-backed idempotency, and full observability via Prometheus & Grafana.

---

## 📋 Prerequisites

| Tool    | Version  |
|---------|----------|
| Java    | 21 (LTS) |
| Maven   | 3.9+     |
| Docker  | 24+      |

---

## 🚀 Quickstart (Local Development)

```bash
# 1. Start infrastructure (Postgres, Redis, Kafka, Prometheus, Grafana)
docker compose up -d

# 2. Build the project
./mvnw clean install -DskipTests

# 3. Run the application
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API will be available at **http://localhost:8080**

---

## 🧪 Running Tests

```bash
# Unit tests
./mvnw test

# Integration tests (requires Docker for Testcontainers)
./mvnw verify
```

---

## 🏗️ Module Overview

| Module               | Package                                      | Responsibility                                                                                            |
|----------------------|----------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| **API Gateway**      | `com.payroute.orchestrate.apigateway`         | Receives inbound payment requests, validates input, enforces idempotency via Redis                        |
| **Orchestrator**     | `com.payroute.orchestrate.orchestrator`       | Applies routing strategy, delegates to provider adapters, handles retries and fallback, publishes events   |
| **Provider Adapter** | `com.payroute.orchestrate.provideradapter`    | One adapter per provider — translates internal request model into provider-specific API contracts          |
| **Event Processor**  | `com.payroute.orchestrate.eventprocessor`     | Consumes Kafka events, updates transaction state, schedules retries, feeds Prometheus metrics              |

---

## 📊 Observability

| Service      | URL                                          |
|--------------|----------------------------------------------|
| Swagger UI   | http://localhost:8080/swagger-ui.html         |
| Prometheus   | http://localhost:9090                         |
| Grafana      | http://localhost:3000 (admin / admin)         |
| Actuator     | http://localhost:8080/actuator                |

---

## 📁 Project Structure

```
orchestrate/
├── src/main/java/com/payroute/orchestrate/
│   ├── apigateway/        # Controllers, filters, validators
│   ├── orchestrator/      # Routing service, strategies, fallback
│   ├── provideradapter/   # Provider adapters, clients, models
│   ├── eventprocessor/    # Kafka consumers, producers, handlers
│   ├── domain/            # Entities, DTOs, enums, exceptions
│   ├── repository/        # Spring Data JPA repositories
│   ├── config/            # Kafka, Redis, security, observability config
│   └── common/            # Constants and utility classes
├── src/main/resources/
│   ├── db/migration/      # Flyway SQL migrations
│   ├── application.yml    # Base configuration
│   ├── application-dev.yml
│   └── application-prod.yml
├── monitoring/            # Prometheus configuration
├── docker-compose.yml     # Local infrastructure
├── Dockerfile             # Multi-stage build
└── pom.xml
```

---

## 📄 License

This project is for educational and portfolio purposes.
