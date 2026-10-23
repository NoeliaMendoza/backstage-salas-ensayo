package org.example.actividad.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.*;
import java.util.*;

@RestControllerAdvice
public class BackstageExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(BackstageExceptionHandler.class);
    private final Clock clock;

    public BackstageExceptionHandler(Clock clock) { this.clock = clock; }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorBackstage> validacion(MethodArgumentNotValidException ex,
                                                    HttpServletRequest request) {
        Map<String, List<String>> detalles = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> detalles
                .computeIfAbsent(error.getField(), campo -> new ArrayList<>())
                .add(error.getDefaultMessage()));
        return responder(HttpStatus.BAD_REQUEST, "VALIDACION_FALLIDA",
                "¡La solicitud tiene notas fuera de tono!", request, detalles);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorBackstage> noEncontrado(RecursoNoEncontradoException ex,
                                                      HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(PaseBackstageInvalidoException.class)
    public ResponseEntity<ErrorBackstage> pase(PaseBackstageInvalidoException ex,
                                             HttpServletRequest request) {
        return responder(HttpStatus.UNAUTHORIZED, "PASE_INVALIDO", ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(LimiteEnsayoExcedidoException.class)
    public ResponseEntity<ErrorBackstage> limite(LimiteEnsayoExcedidoException ex,
                                               HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, "LIMITE_ENSAYO_EXCEDIDO", ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(ConflictoReservaException.class)
    public ResponseEntity<ErrorBackstage> conflicto(ConflictoReservaException ex,
                                                  HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, "CONFLICTO_RESERVA", ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorBackstage> formato(Exception ex, HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA",
                "¡Revisa la partitura! El JSON, la fecha o los identificadores no son válidos.", request, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorBackstage> inesperado(Exception ex, HttpServletRequest request) {
        log.error("Fallo inesperado en {}", request.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "El amplificador falló. Intenta nuevamente más tarde.", request, Map.of());
    }

    private ResponseEntity<ErrorBackstage> responder(HttpStatus status, String codigo,
            String mensaje, HttpServletRequest request, Map<String, List<String>> detalles) {
        return ResponseEntity.status(status).body(new ErrorBackstage(Instant.now(clock),
                status.value(), codigo, mensaje, request.getRequestURI(), detalles));
    }
}
