package com.gridops.microkernel.host;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gridOpsAlertRuleOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("GridOps Alert Rule Engine API")
                .description("Microkernel plugin API: evaluate events, list/add/remove rule plugins.")
                .version("1.0"));
    }
}
