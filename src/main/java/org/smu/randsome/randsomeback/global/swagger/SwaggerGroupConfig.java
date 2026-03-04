package org.smu.randsome.randsomeback.global.swagger;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SwaggerGroupConfig {

    private final SwaggerCustomizer swaggerCustomizer;

    @Bean
    public GroupedOpenApi appApi() {
        return GroupedOpenApi.builder()
                .group("1. App API")
                .pathsToMatch("/v1/**")
                .pathsToExclude("/v1/admin/**")
                .addOperationCustomizer(swaggerCustomizer)
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("2. Admin API")
                .pathsToMatch("/v1/admin/**")
                .addOperationCustomizer(swaggerCustomizer)
                .build();
    }

}