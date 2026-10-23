package org.example.actividad.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingServicioAspect {
    private static final Logger log = LoggerFactory.getLogger(LoggingServicioAspect.class);

    @Around("execution(public * org.example.actividad.service..*(..))")
    public Object medir(ProceedingJoinPoint punto) throws Throwable {
        long inicio = System.nanoTime();
        try {
            return punto.proceed();
        } finally {
            log.info("Servicio {}: {} ms", punto.getSignature().toShortString(),
                    (System.nanoTime() - inicio) / 1_000_000);
        }
    }
}
