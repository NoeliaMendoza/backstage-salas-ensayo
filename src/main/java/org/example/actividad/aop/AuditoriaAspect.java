package org.example.actividad.aop;

import org.aspectj.lang.annotation.*;
import org.example.actividad.repository.AuditoriaRepository;
import org.springframework.stereotype.Component;
import java.time.*;

@Aspect
@Component
public class AuditoriaAspect {
    private final AuditoriaRepository auditoria;
    private final Clock clock;

    public AuditoriaAspect(AuditoriaRepository auditoria, Clock clock) {
        this.auditoria = auditoria;
        this.clock = clock;
    }

    @AfterReturning(pointcut = "@annotation(auditable)", argNames = "auditable")
    public void registrar(Auditable auditable) {
        // No atribuye identidad a una persona: el pase es compartido.
        auditoria.registrar(auditable.value(), Instant.now(clock));
    }
}
