package org.example.actividad.service;

import org.example.actividad.aop.*;
import org.example.actividad.dto.CrearReservaRequest;
import org.example.actividad.exception.*;
import org.example.actividad.model.*;
import org.example.actividad.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
public class ReservaService {
    private final ReservaRepository reservas;
    private final BandaService bandas;
    private final SalaService salas;
    private final ConfirmacionReservaService confirmaciones;
    private final Clock clock;

    public ReservaService(ReservaRepository reservas, BandaService bandas, SalaService salas,
                          ConfirmacionReservaService confirmaciones, Clock clock) {
        this.reservas = reservas;
        this.bandas = bandas;
        this.salas = salas;
        this.confirmaciones = confirmaciones;
        this.clock = clock;
    }

    @ControlarHorasEnsayo
    @Auditable("CREAR_RESERVA")
    public Reserva crear(CrearReservaRequest solicitud) {
        // El aspecto mantiene el bloqueo del repositorio hasta finalizar el guardado.
        Banda banda = bandas.buscar(solicitud.bandaId());
        Sala sala = salas.buscar(solicitud.salaId());
        if (!banda.activa() || !sala.habilitada() || banda.numeroIntegrantes() > sala.capacidad()) {
            throw new ConflictoReservaException("¡La banda o la sala no están listas para este ensayo!");
        }
        int inicio = solicitud.horaInicio().getHour();
        int fin = inicio + solicitud.duracionHoras();
        if (solicitud.horaInicio().getMinute() != 0 || solicitud.horaInicio().getSecond() != 0
                || solicitud.horaInicio().getNano() != 0 || fin > 24
                || solicitud.fecha().isBefore(LocalDate.now(clock))) {
            throw new ConflictoReservaException("Reserva horas exactas, sin pasar de medianoche ni viajar al pasado.");
        }
        boolean ocupada = reservas.confirmadasPorSalaYFecha(sala.id(), solicitud.fecha()).stream()
                .anyMatch(r -> inicio < r.horaInicio().getHour() + r.duracionHoras()
                        && fin > r.horaInicio().getHour());
        if (ocupada) throw new ConflictoReservaException("¡Esa sala ya está haciendo ruido en ese horario!");

        Reserva reserva = reservas.guardar(new Reserva(UUID.randomUUID(), banda.id(), sala.id(),
                solicitud.fecha(), solicitud.horaInicio(), solicitud.duracionHoras(),
                sala.tarifaHora().multiply(BigDecimal.valueOf(solicitud.duracionHoras())),
                EstadoReserva.CONFIRMADA, Instant.now(clock)));
        confirmaciones.confirmar(banda.correoRepresentante(), reserva.id());
        return reserva;
    }

    public Reserva buscar(UUID id) {
        return reservas.buscar(id).orElseThrow(() ->
                new RecursoNoEncontradoException("Esa reserva no está en la lista del concierto."));
    }

    public List<Reserva> porBandaYFecha(UUID bandaId, LocalDate fecha) {
        bandas.buscar(bandaId);
        return reservas.porBandaYFecha(bandaId, fecha);
    }

    @Auditable("CANCELAR_RESERVA")
    public Reserva cancelar(UUID id) {
        synchronized (reservas) {
            return reservas.guardar(buscar(id).cancelar());
        }
    }
}
