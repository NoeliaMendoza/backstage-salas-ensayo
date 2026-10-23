package org.example.actividad.interceptor;

import jakarta.servlet.http.*;
import org.slf4j.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoggingHttpInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(LoggingHttpInterceptor.class);
    private static final String INICIO = LoggingHttpInterceptor.class.getName() + ".inicio";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(INICIO, System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long inicio = (Long) request.getAttribute(INICIO);
        if (inicio != null) {
            log.info("HTTP {} {} -> {} en {} ms", request.getMethod(), request.getRequestURI(),
                    response.getStatus(), (System.nanoTime() - inicio) / 1_000_000);
        }
    }
}
