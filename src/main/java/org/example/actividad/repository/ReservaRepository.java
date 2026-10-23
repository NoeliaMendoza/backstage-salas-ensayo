package org.example.actividad.repository;

import org.example.actividad.model.*;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@org.springframework.context.annotation.Profile("!h2")
public class ReservaRepository {
    private final Map<UUID, Reserva> reservas = new ConcurrentHashMap<>();

    public Optional<Reserva> buscar(UUID id) { return Optional.ofNullable(reservas.get(id)); }
    public Reserva guardar(Reserva reserva) { reservas.put(reserva.id(), reserva); return reserva; }

    public List<Reserva> porBandaYFecha(UUID bandaId, LocalDate fecha) {
        return reservas.values().stream()
                .filter(r -> r.bandaId().equals(bandaId) && r.fecha().equals(fecha))
                .sorted(Comparator.comparing(Reserva::horaInicio)).toList();
    }

    public List<Reserva> confirmadasPorSalaYFecha(UUID salaId, LocalDate fecha) {
        return reservas.values().stream()
                .filter(r -> r.salaId().equals(salaId) && r.fecha().equals(fecha)
                        && r.estado() == EstadoReserva.CONFIRMADA).toList();
    }

    public int sumarHorasConfirmadas(UUID bandaId, LocalDate fecha) {
        return porBandaYFecha(bandaId, fecha).stream()
                .filter(r -> r.estado() == EstadoReserva.CONFIRMADA)
                .mapToInt(Reserva::duracionHoras).sum();
    }
}
