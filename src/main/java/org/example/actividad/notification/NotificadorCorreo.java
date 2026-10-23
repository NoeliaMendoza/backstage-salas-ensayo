package org.example.actividad.notification;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component("notificadorCorreo")
public class NotificadorCorreo implements NotificadorReserva {
    private final JavaMailSender mailSender;
    public NotificadorCorreo(JavaMailSender mailSender) { this.mailSender = mailSender; }
    @Override
    public void notificar(String destinatario, String mensaje) {
        var correo = new SimpleMailMessage();
        correo.setTo(destinatario);
        correo.setSubject("Backstage: tu próximo ensayo");
        correo.setText(mensaje);
        mailSender.send(correo);
    }
}
