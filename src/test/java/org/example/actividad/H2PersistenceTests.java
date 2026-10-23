package org.example.actividad;

import org.example.actividad.config.H2Config;
import org.example.actividad.model.*;
import org.example.actividad.repository.jdbc.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class H2PersistenceTests {
    @TempDir Path directorio;

    @Test
    void archivoConservaDatosAlReconectarEInicializarOtraVez() {
        String url = "jdbc:h2:file:" + directorio.resolve("backstage").toAbsolutePath();
        var config = new H2Config();
        var jdbc = new JdbcTemplate(config.dataSource(url, "sa", ""));
        var banda = new Banda(UUID.randomUUID(), "BAND-9001", "Los Persistentes", "Rock",
                "ensayo@example.com", 4, true);
        var sala = new Sala(UUID.randomUUID(), "Garaje Eléctrico", 6,
                new BigDecimal("12.50"), "Batería y amplificadores", true);
        var reserva = new Reserva(UUID.randomUUID(), banda.id(), sala.id(),
                LocalDate.of(2027, 5, 20), LocalTime.of(10, 0), 4,
                new BigDecimal("50.00"), EstadoReserva.CONFIRMADA,
                Instant.parse("2026-10-22T12:00:00Z"));
        new JdbcBandaRepository(jdbc).guardar(banda);
        new JdbcSalaRepository(jdbc).guardar(sala);
        new JdbcReservaRepository(jdbc).guardar(reserva);
        new JdbcAuditoriaRepository(jdbc).registrar("CREAR_RESERVA", reserva.creadaEn());

        // Cada operación cierra su conexión. Otra fuente reabre el mismo archivo.
        var reconexion = new JdbcTemplate(config.dataSource(url, "sa", ""));
        assertEquals(banda, new JdbcBandaRepository(reconexion).buscar(banda.id()).orElseThrow());
        assertEquals(sala, new JdbcSalaRepository(reconexion).buscar(sala.id()).orElseThrow());
        var reservas = new JdbcReservaRepository(reconexion);
        assertEquals(reserva, reservas.buscar(reserva.id()).orElseThrow());
        assertEquals(4, reservas.sumarHorasConfirmadas(banda.id(), reserva.fecha()));
        reservas.guardar(reserva.cancelar());
        assertEquals(0, reservas.sumarHorasConfirmadas(banda.id(), reserva.fecha()));
        assertEquals(1, new JdbcAuditoriaRepository(reconexion).listar().size());
    }
}
