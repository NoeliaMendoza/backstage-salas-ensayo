package org.example.actividad.model;

import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

public record Reserva(UUID id, UUID bandaId, UUID salaId, LocalDate fecha,
                      LocalTime horaInicio, int duracionHoras, BigDecimal total,
                      EstadoReserva estado, Instant creadaEn) {
    public Reserva cancelar() {
        return new Reserva(id, bandaId, salaId, fecha, horaInicio, duracionHoras,
                total, EstadoReserva.CANCELADA, creadaEn);
    }
}
