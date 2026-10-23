package org.example.actividad;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Repite todos los contratos y reglas de negocio con repositorios SQL reales. */
@ActiveProfiles("h2")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "backstage.datasource.url=jdbc:h2:mem:backstage-tests;DB_CLOSE_DELAY=-1")
class H2IntegrationTests extends BackstageIntegrationTests {}
