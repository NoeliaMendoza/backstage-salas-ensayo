package org.example.actividad.repository.jdbc;

import org.example.actividad.model.*;
import org.example.actividad.repository.ReservaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;
import java.time.*;
import java.util.*;

@Repository
@Profile("h2")
public class JdbcReservaRepository extends ReservaRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<Reserva> mapper = (rs, fila) -> new Reserva(
            rs.getObject("id", UUID.class), rs.getObject("banda_id", UUID.class),
            rs.getObject("sala_id", UUID.class), rs.getObject("fecha", LocalDate.class),
            rs.getObject("hora_inicio", LocalTime.class), rs.getInt("duracion_horas"),
            rs.getBigDecimal("total"), EstadoReserva.valueOf(rs.getString("estado")),
            rs.getObject("creada_en", OffsetDateTime.class).toInstant());

    public JdbcReservaRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public Optional<Reserva> buscar(UUID id) {
        return jdbc.query("SELECT * FROM reservas WHERE id = ?", mapper, id).stream().findFirst();
    }

    @Override
    public Reserva guardar(Reserva reserva) {
        jdbc.update("""
                MERGE INTO reservas (id,banda_id,sala_id,fecha,hora_inicio,duracion_horas,total,estado,creada_en)
                KEY(id) VALUES (?,?,?,?,?,?,?,?,?)
                """, reserva.id(), reserva.bandaId(), reserva.salaId(), reserva.fecha(),
                reserva.horaInicio(), reserva.duracionHoras(), reserva.total(), reserva.estado().name(),
                reserva.creadaEn().atOffset(ZoneOffset.UTC));
        return reserva;
    }

    @Override
    public List<Reserva> porBandaYFecha(UUID bandaId, LocalDate fecha) {
        return jdbc.query("SELECT * FROM reservas WHERE banda_id = ? AND fecha = ? ORDER BY hora_inicio",
                mapper, bandaId, fecha);
    }

    @Override
    public List<Reserva> confirmadasPorSalaYFecha(UUID salaId, LocalDate fecha) {
        return jdbc.query("SELECT * FROM reservas WHERE sala_id = ? AND fecha = ? AND estado = 'CONFIRMADA'",
                mapper, salaId, fecha);
    }

    @Override
    public int sumarHorasConfirmadas(UUID bandaId, LocalDate fecha) {
        return jdbc.queryForObject("""
                SELECT COALESCE(SUM(duracion_horas),0) FROM reservas
                WHERE banda_id = ? AND fecha = ? AND estado = 'CONFIRMADA'
                """, Integer.class, bandaId, fecha);
    }
}
