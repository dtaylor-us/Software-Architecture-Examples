package com.example.spacebased.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI spaceBasedEnergyApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Space-Based Energy API")
                .description("Energy operations price updates and spike alerts. Redis is the shared space; API is stateless.")
                .version("1.0"));
    }
}
