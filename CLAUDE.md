# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
mvn clean package              # Build the project
mvn package -DskipTests        # Build without running tests
mvn test                       # Run all tests
mvn test -Dtest=ClassName      # Run a single test class
mvn test -Dtest=ClassName#method  # Run a single test method
mvn deploy -DskipTests         # Publish to GitHub Packages
```

## Architecture Overview

Spring Boot 2.7.5 / Java 11 backend for restaurant kitchen order management. Uses a **hybrid database architecture**:

- **MongoDB** (`kitchenmgmt` db): Primary domain storage — plates, categories, kitchen menu items. Accessed via Spring Data MongoDB.
- **PostgreSQL** (dual datasource): Transactional data. The `postgres` datasource uses the async `pgjdbc-ng` driver; the `ack` datasource uses the standard JDBC driver via Hikari. Schema is managed manually via SQL scripts in `src/main/resources/postgres/` (no Flyway/Liquibase).

### Layer Structure (package `com.bbc.km`)

- **Generic base classes** drive the CRUD layer:
  - `MongoDocument<ID>` — base MongoDB document with audit timestamps
  - `CRUDService<ID, DTO>` — base service; subclasses override `validateOnCreate()` / `validateOnUpdate()`
  - `RESTController<ID, DTO>` — base REST controller exposing standard GET/POST/PUT/DELETE endpoints
- **Concrete domain classes**: `Plate`, `Category`, `KitchenMenuItem`, `PlateKitchenMenuItem`, `Stats`
- **JPA layer** (`com.bbc.km.jpa`): `Articolo`, `OrderAck`, `Tipologia` entities + `OrderAckProcessingJob` scheduled background job (configurable via `application.jobs.order-ack.*`)
- **WebSocket** (`com.bbc.km.websocket`): STOMP over `/ws` (SockJS fallback), topics under `/topic` for real-time plate status pushes
- **Integration** (`com.bbc.km.integration`): `GSGIntegrationService` / `GSGIntegrationController` for external system integration
- **PostgreSQL triggers** (`src/main/resources/postgres/`): Notify-based triggers that push order events as JSON payloads; the app listens and processes them via `OrderAckProcessingJob`

### Key Configuration (`src/main/resources/application.yaml`)

```yaml
application:
  enable-orders-auto-insert: false   # toggle auto-insert of orders into queue
  menu-item-notes-separator: /       # separator used when parsing item notes
  jobs:
    order-ack:
      fixedDelay: 60000              # ms delay between job executions
      interval: 120000
```

The dual PostgreSQL datasources are configured under `spring.data.postgres` and wired explicitly in `PostgresConfig` / `JpaConfig`.

### Tests

Unit tests (JUnit 5 + Mockito) for the service layer only — `PlateServiceTest`, `CategoryServiceTest`, `KitchenMenuItemServiceTest`, `StatsServiceTest`. No integration or container-based tests exist. Tests focus on validation logic inside `validateOnCreate()` / `validateOnUpdate()`.

### CI/CD

GitHub Actions (`.github/workflows/maven-publish.yml`): triggered on release or manual dispatch. Builds with `mvn package -DskipTests` then publishes to GitHub Packages with `mvn deploy -DskipTests`.