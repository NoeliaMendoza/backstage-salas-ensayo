package org.example.actividad.repository.jdbc;

import org.example.actividad.model.Sala;
import org.example.actividad.repository.SalaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
@Profile("h2")
public class JdbcSalaRepository extends SalaRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<Sala> mapper = (rs, fila) -> new Sala(
            rs.getObject("id", UUID.class), rs.getString("nombre"), rs.getInt("capacidad"),
            rs.getBigDecimal("tarifa_hora"), rs.getString("descripcion_equipo"), rs.getBoolean("habilitada"));

    public JdbcSalaRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public Optional<Sala> buscar(UUID id) {
        return jdbc.query("SELECT * FROM salas WHERE id = ?", mapper, id).stream().findFirst();
    }

    @Override
    public List<Sala> listarHabilitadas() {
        return jdbc.query("SELECT * FROM salas WHERE habilitada = TRUE ORDER BY nombre", mapper);
    }

    @Override
    public Sala guardar(Sala sala) {
        jdbc.update("""
                MERGE INTO salas (id,nombre,capacidad,tarifa_hora,descripcion_equipo,habilitada)
                KEY(id) VALUES (?,?,?,?,?,?)
                """, sala.id(), sala.nombre(), sala.capacidad(), sala.tarifaHora(),
                sala.descripcionEquipo(), sala.habilitada());
        return sala;
    }
}
