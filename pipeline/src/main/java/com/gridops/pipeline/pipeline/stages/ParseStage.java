package com.gridops.pipeline.pipeline.stages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gridops.pipeline.domain.TelemetryEventType;
import com.gridops.pipeline.domain.TelemetryPipelineContext;
import com.gridops.pipeline.pipeline.PipelineStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Stage 2 – Parse.
 *
 * <p>Detects the input format and extracts structured fields into the context.
 *
 * <p><b>Supported formats:</b>
 * <ul>
 *   <li><b>JSON blob</b> – input starts with {@code {}<br>
 *       {@code {"type":"OUTAGE","deviceId":"DEV-001","timestamp":"2024-01-15T10:30:00Z","value":1500.0,"region":"WEST"}}
 *   <li><b>Pipe-delimited line</b> – {@code TYPE|DEVICE_ID|TIMESTAMP|VALUE[|REGION]}
 * </ul>
 */
@Component
@Order(2)
public class ParseStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(ParseStage.class);

    private final ObjectMapper objectMapper;

    public ParseStage(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void process(TelemetryPipelineContext context) {
        String raw = context.getRawInput();
        log.debug("[Parse] Parsing input");

        try {
            if (raw.startsWith("{")) {
                parseJson(raw, context);
            } else {
                parsePipeDelimited(raw, context);
            }
            log.debug("[Parse] Detected type={}, deviceId={}", context.getEventType(), context.getDeviceId());
        } catch (Exception e) {
            context.addValidationError("Parse error: " + e.getMessage());
            log.warn("[Parse] Failed to parse input: {}", e.getMessage());
        }
    }

    private void parseJson(String raw, TelemetryPipelineContext context) throws Exception {
        JsonNode node = objectMapper.readTree(raw);
        context.setEventType(resolveType(node.path("type").asText("")));
        context.setDeviceId(node.path("deviceId").asText(null));
        context.setValue(node.path("value").asDouble(0.0));
        context.setUnit(node.path("unit").asText(null));
        context.setRegion(node.path("region").asText(null));

        String ts = node.path("timestamp").asText(null);
        if (ts != null && !ts.isBlank()) {
            context.setTimestamp(parseTimestamp(ts));
        }
    }

    private void parsePipeDelimited(String raw, TelemetryPipelineContext context) {
        String[] parts = raw.split("\\|", -1);
        if (parts.length < 4) {
            context.addValidationError("Pipe-delimited input requires at least 4 fields: TYPE|DEVICE_ID|TIMESTAMP|VALUE");
            return;
        }
        context.setEventType(resolveType(parts[0].trim()));
        context.setDeviceId(parts[1].trim());
        context.setTimestamp(parseTimestamp(parts[2].trim()));
        context.setValue(Double.parseDouble(parts[3].trim()));
        if (parts.length >= 5 && !parts[4].isBlank()) {
            context.setRegion(parts[4].trim());
        }
    }

    private TelemetryEventType resolveType(String raw) {
        try {
            return TelemetryEventType.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TelemetryEventType.UNKNOWN;
        }
    }

    private Instant parseTimestamp(String ts) {
        try {
            return Instant.parse(ts);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid timestamp format (expected ISO-8601): " + ts);
        }
    }

    @Override
    public String name() {
        return "Parse";
    }
}
