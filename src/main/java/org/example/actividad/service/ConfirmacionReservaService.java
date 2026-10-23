package org.example.actividad.service;

import org.example.actividad.notification.NotificadorReserva;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class ConfirmacionReservaService {
    private static final Logger log = LoggerFactory.getLogger(ConfirmacionReservaService.class);
    private final NotificadorReserva notificador;

    public ConfirmacionReservaService(@Qualifier("notificadorConsola") NotificadorReserva notificador) {
        this.notificador = notificador;
    }

    public void confirmar(String correo, UUID reservaId) {
        try {
            notificador.notificar(correo, "¡Amplificadores listos! Tu reserva es " + reservaId);
        } catch (RuntimeException ex) {
            // El ensayo permanece confirmado aunque falle la notificación.
            log.warn("No se pudo notificar la reserva {}", reservaId, ex);
        }
    }
}
