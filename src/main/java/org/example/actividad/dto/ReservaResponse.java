package org.example.actividad.dto;

import org.example.actividad.model.EstadoReserva;
import org.example.actividad.model.Reserva;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/** Contrato público de reservas, separado de la entidad almacenada en memoria. */
public record ReservaResponse(UUID id, UUID bandaId, UUID salaId, LocalDate fecha,
                              LocalTime horaInicio, int duracionHoras, BigDecimal total,
                              EstadoReserva estado, Instant creadaEn) {

    public static ReservaResponse desde(Reserva reserva) {
        return new ReservaResponse(reserva.id(), reserva.bandaId(), reserva.salaId(),
                reserva.fecha(), reserva.horaInicio(), reserva.duracionHoras(),
                reserva.total(), reserva.estado(), reserva.creadaEn());
    }
}
