package org.smu.randsome.randsomeback.global.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String AUTH_SCHEME = "Bearer ";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(globalSecurityRequirement())
                .components(components())
                .servers(List.of(
                        server("Local Server", "http://localhost:8080")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("RandSome API Docs")
                .description("RandSome 서비스 API 문서입니다.")
                .version("1.0.0");
    }

    private SecurityRequirement globalSecurityRequirement() {
        return new SecurityRequirement().addList(AUTH_SCHEME);
    }

    private Components components() {
        return new Components()
                .addSecuritySchemes(AUTH_SCHEME, bearerSecurityScheme())
                .addSchemas("ErrorResponse", errorResponseSchema());
    }

    private SecurityScheme bearerSecurityScheme() {
        return new SecurityScheme()
                .name(AUTH_SCHEME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("Bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description("JWT 토큰을 입력하세요. (Bearer 제외)");
    }

    private Schema<?> errorResponseSchema() {
        return new Schema<>()
                .addProperty("code", new StringSchema())
                .addProperty("message", new StringSchema())
                .addProperty("data", new Schema<>());
    }

    private Server server(String description, String url) {
        return new Server().description(description).url(url);
    }

}