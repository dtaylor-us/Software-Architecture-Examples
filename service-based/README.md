# GridOps Service-Based Architecture Example

Educational implementation of **Service-Based Architecture** (Richards/Ford) in the GridOps domain. Single Spring Boot deployable with five coarse-grained services communicating **in-process only**.

## Services

| Service          | Package            | Responsibility                    |
|------------------|--------------------|-----------------------------------|
| OutageService    | `com.gridops.outage`   | Report and query outages          |
| ForecastService  | `com.gridops.forecast` | Create and query load forecasts   |
| PricingService   | `com.gridops.pricing`  | Price rules and zone prices       |
| AlertService     | `com.gridops.alert`    | Raise and list alerts             |
| AuditService     | `com.gridops.audit`    | Audit log (record and query)      |

## Quick start

```bash
./mvnw spring-boot:run
```

Then try the [sample cURL flows](docs/CURL_EXAMPLES.md).

**Data:** Spring Data JPA with **H2** (in-memory by default). The H2 console is at [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:gridops`, user: `sa`, password blank). Each service has a JPA entity and a Spring Data repository; the app uses `Jpa*Repository` implementations. Unit tests use in-memory repository implementations (no DB).

## Architecture

- **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** — What service-based is, tradeoffs vs microservices, keeping boundaries strong, Mermaid diagram, and how to evolve to microservices.

## Tests

Each service is testable in isolation (no HTTP, no other services):

```bash
./mvnw test
```

- `OutageServiceTest`
- `ForecastServiceTest`
- `PricingServiceTest`
- `AlertServiceTest`
- `AuditServiceTest`

## Project layout

```
src/main/java/com/gridops/
├── GridOpsApplication.java
├── api/                    # REST controllers + orchestration
│   ├── OutageController.java
│   ├── ForecastController.java
│   ├── PricingController.java
│   ├── AlertController.java
│   ├── AuditController.java
│   └── GridOpsOrchestrationController.java
├── outage/                 # OutageService, OutageEntity, OutageRepository (JPA + in-memory)
├── forecast/
├── pricing/
├── alert/
└── audit/
```

## License

Educational use.
