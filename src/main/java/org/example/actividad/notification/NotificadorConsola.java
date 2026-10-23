package org.example.actividad.notification;

import org.slf4j.*;
import org.springframework.stereotype.Component;

@Component("notificadorConsola")
public class NotificadorConsola implements NotificadorReserva {
    private static final Logger log = LoggerFactory.getLogger(NotificadorConsola.class);
    @Override
    public void notificar(String destinatario, String mensaje) {
        log.info("Aviso Backstage para {}: {}", destinatario, mensaje);
    }
}
