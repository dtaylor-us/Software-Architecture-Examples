package com.gridops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Single deployable for the Service-Based Architecture example.
 * Internal services (Outage, Forecast, Pricing, Alert, Audit) run in-process.
 */
@SpringBootApplication
public class GridOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GridOpsApplication.class, args);
    }
}
