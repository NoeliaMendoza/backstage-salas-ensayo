package org.example.actividad.interceptor;

import jakarta.servlet.http.*;
import org.example.actividad.exception.PaseBackstageInvalidoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class BackstagePassInterceptor implements HandlerInterceptor {
    private final String pase;

    public BackstagePassInterceptor(@Value("${backstage.pase}") String pase) { this.pase = pase; }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        // Solamente GET es público; crear una sala siempre exige pase.
        if ("GET".equals(request.getMethod())
                && (ruta.equals("/api/salas") || ruta.matches("/api/salas/[^/]+"))) return true;

        if (!pase.equals(request.getHeader("X-Backstage-Pass"))) {
            throw new PaseBackstageInvalidoException("¡Alto ahí, rockstar! Tu pase Backstage no es válido.");
        }
        return true;
    }
}
