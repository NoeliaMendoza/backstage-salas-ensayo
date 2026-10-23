package org.example.actividad.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.example.actividad.config.PoliticaEnsayo;
import org.example.actividad.dto.CrearReservaRequest;
import org.example.actividad.exception.LimiteEnsayoExcedidoException;
import org.example.actividad.repository.ReservaRepository;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LimiteHorasEnsayoAspect {
    private final ReservaRepository reservas;
    private final PoliticaEnsayo politica;

    public LimiteHorasEnsayoAspect(ReservaRepository reservas, PoliticaEnsayo politica) {
        this.reservas = reservas;
        this.politica = politica;
    }

    @Around(value = "@annotation(org.example.actividad.aop.ControlarHorasEnsayo) && args(solicitud)",
            argNames = "punto,solicitud")
    public Object verificar(ProceedingJoinPoint punto, CrearReservaRequest solicitud) throws Throwable {
        // Comprobar y crear son una única operación para esta instancia en memoria.
        synchronized (reservas) {
            int horas = reservas.sumarHorasConfirmadas(solicitud.bandaId(), solicitud.fecha());
            if (horas + solicitud.duracionHoras() > politica.maxHorasDiarias()) {
                throw new LimiteEnsayoExcedidoException("¡Bajen el volumen! La banda ya tiene " + horas
                        + " horas y no puede superar 6 en la misma fecha.");
            }
            return punto.proceed();
        }
    }
}
