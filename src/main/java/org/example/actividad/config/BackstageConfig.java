package org.example.actividad.config;

import java.time.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@EnableAspectJAutoProxy
public class BackstageConfig {
    @Bean
    public Clock backstageClock(@Value("${backstage.zona}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }

    @Bean
    public PoliticaEnsayo politicaEnsayo(@Value("${backstage.max-horas-diarias}") int horas) {
        return new PoliticaEnsayo(horas);
    }

    @Bean
    public LocalValidatorFactoryBean validator(Clock clock) {
        var validator = new LocalValidatorFactoryBean();
        // Bean Validation y las reglas del servicio comparten zona horaria.
        validator.setConfigurationInitializer(config -> config.clockProvider(() -> clock));
        return validator;
    }
}
