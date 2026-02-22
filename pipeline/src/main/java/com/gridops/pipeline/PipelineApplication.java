package com.gridops.pipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Pipeline (Pipes &amp; Filters) Architecture example.
 *
 * <p>Raw GridOps telemetry events flow through eight ordered stages:
 * Ingest → Parse → Normalize → Validate → Enrich → Filter → Persist → PublishSummary
 */
@SpringBootApplication
public class PipelineApplication {

    public static void main(String[] args) {
        SpringApplication.run(PipelineApplication.class, args);
    }
}
