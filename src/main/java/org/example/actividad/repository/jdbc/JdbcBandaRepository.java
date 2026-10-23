package org.example.actividad.repository.jdbc;

import org.example.actividad.model.Banda;
import org.example.actividad.repository.BandaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
@Profile("h2")
public class JdbcBandaRepository extends BandaRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<Banda> mapper = (rs, fila) -> new Banda(
            rs.getObject("id", UUID.class), rs.getString("codigo"), rs.getString("nombre"),
            rs.getString("genero_musical"), rs.getString("correo_representante"),
            rs.getInt("numero_integrantes"), rs.getBoolean("activa"));

    public JdbcBandaRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public Optional<Banda> buscar(UUID id) {
        return jdbc.query("SELECT * FROM bandas WHERE id = ?", mapper, id).stream().findFirst();
    }

    @Override
    public boolean existeCodigo(String codigo) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM bandas WHERE codigo = ?", Integer.class, codigo) > 0;
    }

    @Override
    public Banda guardar(Banda banda) {
        jdbc.update("""
                MERGE INTO bandas (id,codigo,nombre,genero_musical,correo_representante,numero_integrantes,activa)
                KEY(id) VALUES (?,?,?,?,?,?,?)
                """, banda.id(), banda.codigo(), banda.nombre(), banda.generoMusical(),
                banda.correoRepresentante(), banda.numeroIntegrantes(), banda.activa());
        return banda;
    }
}
