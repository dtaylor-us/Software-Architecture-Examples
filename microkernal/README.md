# GridOps Alert Rule Engine — Microkernel Example

Educational **microkernel (plugin) architecture** (Richards & Ford) in the **energy planning** domain: a core Alert Rule Engine evaluates normalized GridOps events and delegates to pluggable rule plugins.

## Structure

- **core** — Shared contracts (`RulePlugin` v1), `GridOpsEvent`, `Alert`, `AlertRuleEngine`, `PluginDiscovery` (ServiceLoader).
- **plugins/price-spike** — Fires when `price` exceeds threshold.
- **plugins/forecast-ramp** — Fires when forecast ramp (e.g. `rampMw`) exceeds threshold.
- **plugins/outage-risk** — Fires when reserve margin (e.g. `reserveMarginPct`) falls below threshold.
- **host-app** — Spring Boot app: plugin discovery, `POST /api/evaluate`, `GET /api/plugins`.

## Build and run

```bash
mvn clean install
cd host-app && mvn spring-boot:run
```

Or from repo root:

```bash
mvn -pl host-app spring-boot:run
```

## API

- **Swagger UI** — [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) (when the app is running). OpenAPI spec: `/v3/api-docs`.
- **POST /api/evaluate** — Evaluate a GridOps event. Body: `{ "eventId": "optional", "eventType": "price|forecast-ramp|outage-risk|...", "payload": { "price": 200 } }`. Returns `eventId`, `alerts[]`, `pluginsFired[]`.
- **GET /api/plugins** — List installed plugins (id, name, contractVersion).
- **POST /api/plugins** — Add a plugin at runtime (body: `{ "pluginId": "price-spike" }`).
- **DELETE /api/plugins/{pluginId}** — Remove a plugin at runtime.

## Example

After starting the app (`mvn -pl host-app spring-boot:run`):

```bash
# List plugins
curl -s http://localhost:8080/api/plugins

# Trigger price spike alert (returns eventId, alerts[], pluginsFired[])
curl -s -X POST http://localhost:8080/api/evaluate \
  -H "Content-Type: application/json" \
  -d '{"eventType":"price","payload":{"price":200}}'
```

Example response from `POST /api/evaluate`:

```json
{
  "eventId": "evt-...",
  "alerts": [
    {
      "pluginId": "price-spike",
      "ruleId": "price-spike-rule",
      "severity": "HIGH",
      "message": "Price spike detected: 200.0 >= 150.0",
      "raisedAt": "2025-02-21T..."
    }
  ],
  "pluginsFired": ["price-spike"]
}
```

## Tests

```bash
mvn test
```

- **core**: Engine unit tests and `RulePluginContractTest` (abstract contract tests).
- **plugins**: Each plugin extends the contract test and has domain unit tests.
- **host-app**: Spring Boot integration tests for `/evaluate` and `/plugins`.

## Documentation

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for:

- What the microkernel is and why use plugins
- How contracts keep the core thin
- Strengths/weaknesses and microkernel vs microservices
- ServiceLoader vs explicit registry tradeoffs
- Mermaid component diagram

## Tech stack

- Java 21, Maven multi-module
- Spring Boot 3.2 (host-app only)
- JUnit 5 for tests
