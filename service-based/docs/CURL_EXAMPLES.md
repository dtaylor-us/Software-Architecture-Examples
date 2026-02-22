# Sample cURL Flows

Run the app first: `./mvnw spring-boot:run` (or `mvn spring-boot:run`). Base URL: `http://localhost:8080`.

---

## 1. Report an outage (triggers alert + audit in-process)

```bash
curl -s -X POST http://localhost:8080/api/outages \
  -H "Content-Type: application/json" \
  -d '{
    "assetId": "TRANSFORMER-NORTH-01",
    "description": "Overheating detected",
    "severity": "HIGH"
  }'
```

---

## 2. List active outages

```bash
curl -s http://localhost:8080/api/outages/active
```

---

## 3. Get outage by ID (use id from step 1)

```bash
curl -s http://localhost:8080/api/outages/OUT-xxxxxxxx
```

---

## 4. Create a forecast

```bash
curl -s -X POST http://localhost:8080/api/forecasts \
  -H "Content-Type: application/json" \
  -d '{
    "zoneId": "ZONE-NORTH",
    "mwValue": 250.5,
    "horizon": "DAY-AHEAD"
  }'
```

---

## 5. List forecasts by zone

```bash
curl -s "http://localhost:8080/api/forecasts?zoneId=ZONE-NORTH"
```

---

## 6. Create a price rule

```bash
curl -s -X POST http://localhost:8080/api/pricing/rules \
  -H "Content-Type: application/json" \
  -d '{
    "zoneId": "ZONE-NORTH",
    "effectiveFrom": "2025-02-20T00:00:00Z",
    "effectiveTo": "2025-02-21T00:00:00Z",
    "pricePerMwh": 55.25
  }'
```

---

## 7. Get price for zone at time

```bash
curl -s "http://localhost:8080/api/pricing/price?zoneId=ZONE-NORTH&at=2025-02-20T12:00:00Z"
```

---

## 8. Raise an alert

```bash
curl -s -X POST http://localhost:8080/api/alerts \
  -H "Content-Type: application/json" \
  -d '{
    "type": "PRICE_SPIKE",
    "message": "Price exceeded 200 $/MWh in ZONE-NORTH",
    "severity": "HIGH",
    "sourceId": "ZONE-NORTH"
  }'
```

---

## 9. List active alerts

```bash
curl -s http://localhost:8080/api/alerts/active
```

---

## 10. Record audit entry

```bash
curl -s -X POST http://localhost:8080/api/audit \
  -H "Content-Type: application/json" \
  -d '{
    "actor": "operator-1",
    "action": "REPORT_OUTAGE",
    "targetType": "Outage",
    "targetId": "OUT-abc123",
    "details": "Manual report from SCADA"
  }'
```

---

## 11. List recent audit entries

```bash
curl -s "http://localhost:8080/api/audit/recent?limit=10"
```

---

## 12. Orchestrated flow: outage + alert + audit (single call)

```bash
curl -s -X POST "http://localhost:8080/api/orchestrate/outage-with-alert?assetId=SUBSTATION-A&description=Voltage%20dip&severity=MEDIUM"
```

---

## 13. Dashboard summary (all services in-process)

```bash
curl -s http://localhost:8080/api/orchestrate/dashboard
```

---

## 14. Health check

```bash
curl -s http://localhost:8080/actuator/health
```
