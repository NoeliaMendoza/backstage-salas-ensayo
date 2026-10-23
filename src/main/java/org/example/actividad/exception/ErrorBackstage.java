package org.example.actividad.exception;

import java.time.Instant;
import java.util.*;

public record ErrorBackstage(Instant timestamp, int status, String codigo,
                             String mensaje, String ruta, Map<String, List<String>> detalles) {}
