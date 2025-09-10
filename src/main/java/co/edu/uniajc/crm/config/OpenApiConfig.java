package co.edu.uniajc.crm.config;

import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI crmOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Boossteam CRM – API HU5 (Usuarios)")
                        .version("1.0.0")
                        .description("Endpoints de gestión de usuarios (HU5). Rutas en español."));
    }

    @Bean
    public GroupedOpenApi usuariosGroup() {
        return GroupedOpenApi.builder()
                .group("usuarios")
                .pathsToMatch("/api/users/**")
                .build();
    }
}

