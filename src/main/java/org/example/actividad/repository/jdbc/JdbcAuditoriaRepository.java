package org.example.actividad.repository.jdbc;

import org.example.actividad.repository.AuditoriaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.*;
import java.util.List;

@Repository
@Profile("h2")
public class JdbcAuditoriaRepository extends AuditoriaRepository {
    private final JdbcTemplate jdbc;
    public JdbcAuditoriaRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public void registrar(String accion, Instant fecha) {
        jdbc.update("INSERT INTO auditoria (accion,fecha) VALUES (?,?)",
                accion, fecha.atOffset(ZoneOffset.UTC));
    }

    @Override
    public List<Evento> listar() {
        return jdbc.query("SELECT accion,fecha FROM auditoria ORDER BY id",
                (rs, fila) -> new Evento(rs.getString("accion"),
                        rs.getObject("fecha", OffsetDateTime.class).toInstant()));
    }
}
