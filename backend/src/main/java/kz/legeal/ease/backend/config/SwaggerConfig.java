package kz.legeal.ease.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:9191").description("Local Development"),
                        new Server().url("https://api.legalease.kz").description("Production")
                ))
                .info(new Info()
                        .title("LegalEase API")
                        .version("1.0.0")
                        .description("""
                                AI-powered legal document platform for Kazakhstan.

                                **User roles:**
                                - `ROLE_USER` — create and manage personal legal documents
                                - `ROLE_LAWYER` — create and publish document templates with validation, risk, and matching rules
                                - `ROLE_ADMIN` — manage users, categories, and the rule engine globally

                                **Auth:** All secured endpoints require a Bearer JWT token. \
                                Obtain tokens via `POST /open-api/auth/login`.
                                """)
                        .contact(new Contact()
                                .name("LegalEase Team")
                                .email("support@legalease.kz")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT access token. Prefix with 'Bearer '")));
    }
}