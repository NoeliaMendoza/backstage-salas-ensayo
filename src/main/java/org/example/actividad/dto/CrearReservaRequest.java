package org.example.actividad.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CrearReservaRequest(
        @NotNull UUID bandaId,
        @NotNull UUID salaId,
        @NotNull @FutureOrPresent(message = "¡No podemos ensayar en el pasado!") LocalDate fecha,
        @NotNull LocalTime horaInicio,
        @NotNull @Min(1) @Max(value = 6, message = "¡Una sesión no puede superar seis horas!")
        Integer duracionHoras) {}
