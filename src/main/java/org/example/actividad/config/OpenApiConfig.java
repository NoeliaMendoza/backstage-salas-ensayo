package org.example.actividad.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI backstageOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Backstage — Salas de ensayo").version("1.0")
                        .description("Bandas, salas y reservas. Máximo seis horas por banda y fecha."))
                .components(new Components().addSecuritySchemes("BackstagePass",
                        new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER).name("X-Backstage-Pass")
                                .description("Pase compartido del entorno local.")));
    }

    @Bean
    public OpenApiCustomizer accesoPorOperacion() {
        return api -> api.getPaths().forEach((ruta, item) ->
                item.readOperationsMap().forEach((metodo, operacion) -> {
                    boolean publica = ruta.startsWith("/public/")
                            || (metodo == PathItem.HttpMethod.GET
                                && (ruta.equals("/api/salas") || ruta.equals("/api/salas/{id}")));
                    if (!publica) {
                        operacion.addSecurityItem(new SecurityRequirement().addList("BackstagePass"));
                    }
                }));
    }
}
