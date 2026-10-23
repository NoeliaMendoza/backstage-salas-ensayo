package org.example.actividad.config;

import org.example.actividad.interceptor.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final LoggingHttpInterceptor logging;
    private final BackstagePassInterceptor pase;

    public WebConfig(LoggingHttpInterceptor logging, BackstagePassInterceptor pase) {
        this.logging = logging;
        this.pase = pase;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logging).addPathPatterns("/**")
                .excludePathPatterns("/error", "/favicon.ico").order(0);
        registry.addInterceptor(pase).addPathPatterns("/api/**").order(1);
        // /public/** queda fuera de /api/** y no requiere pase.
    }
}
