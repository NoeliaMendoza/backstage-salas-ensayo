package org.example.actividad;

import jakarta.validation.Validator;
import org.example.actividad.dto.*;
import org.example.actividad.exception.*;
import org.example.actividad.model.*;
import org.example.actividad.repository.AuditoriaRepository;
import org.example.actividad.service.*;
import org.example.actividad.controller.ReservaController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BackstageIntegrationTests {
    private static final AtomicInteger CODIGO = new AtomicInteger(1000);
    @Autowired BandaService bandas;
    @Autowired SalaService salas;
    @Autowired ReservaService reservas;
    @Autowired AuditoriaRepository auditoria;
    @Autowired Validator validator;
    @Autowired Environment environment;
    @Autowired Clock clock;
    @Autowired ReservaController reservaController;

    private Banda banda() {
        return bandas.crear(new CrearBandaRequest("BAND-" + CODIGO.incrementAndGet(),
                "Los Acordes Perdidos", "Rock", "ensayo@example.com", 4));
    }

    private Sala sala() {
        return salas.crear(new CrearSalaRequest("Escenario Violeta", 6,
                new BigDecimal("12.50"), "Batería y amplificadores"));
    }

    private CrearReservaRequest solicitud(Banda banda, Sala sala, int inicio, int horas) {
        return new CrearReservaRequest(banda.id(), sala.id(), LocalDate.now(clock).plusDays(1),
                LocalTime.of(inicio, 0), horas);
    }

    @Test
    void seisHorasSonValidasYCancelarLiberaElCupo() {
        Banda banda = banda();
        Sala sala = sala();
        Reserva primera = reservas.crear(solicitud(banda, sala, 10, 4));
        assertEquals(new BigDecimal("50.00"), primera.total());
        Reserva segunda = reservas.crear(solicitud(banda, sala, 14, 2));
        long auditoriasAntes = auditoria.listar().size();

        // Otra sala tampoco permite superar las seis horas de la banda.
        Sala otraSala = sala();
        assertThrows(LimiteEnsayoExcedidoException.class,
                () -> reservas.crear(solicitud(banda, otraSala, 16, 1)));
        assertEquals(auditoriasAntes, auditoria.listar().size());

        assertEquals(EstadoReserva.CANCELADA, reservas.cancelar(segunda.id()).estado());
        assertEquals(EstadoReserva.CANCELADA, reservas.cancelar(segunda.id()).estado());
        assertDoesNotThrow(() -> reservas.crear(solicitud(banda, sala, 14, 2)));
        var siguienteDia = new CrearReservaRequest(banda.id(), otraSala.id(),
                LocalDate.now(clock).plusDays(2), LocalTime.of(10, 0), 6);
        assertDoesNotThrow(() -> reservas.crear(siguienteDia));
    }

    @Test
    void impideSuperposicionesPeroPermiteHorariosConsecutivos() {
        Sala sala = sala();
        Banda primera = banda();
        Banda segunda = banda();
        reservas.crear(solicitud(primera, sala, 10, 2));
        assertThrows(ConflictoReservaException.class,
                () -> reservas.crear(solicitud(segunda, sala, 11, 2)));
        assertDoesNotThrow(() -> reservas.crear(solicitud(segunda, sala, 12, 2)));
    }

    @Test
    void validaCodigoDeBanda() {
        assertFalse(validator.validate(new CrearBandaRequest("ROCK-1234", "Banda",
                "Rock", "banda@example.com", 4)).isEmpty());
        assertTrue(validator.validate(new CrearBandaRequest("BAND-1234", "Banda",
                "Rock", "banda@example.com", 4)).isEmpty());
    }

    @Test
    void solicitudesSimultaneasNoSuperanElLimite() throws Exception {
        Banda banda = banda();
        Sala primera = sala();
        Sala segunda = sala();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            List<Future<Boolean>> resultados = new ArrayList<>();
            for (Sala sala : List.of(primera, segunda)) {
                resultados.add(pool.submit(() -> {
                    inicio.await();
                    try {
                        reservas.crear(solicitud(banda, sala, 10, 4));
                        return true;
                    } catch (LimiteEnsayoExcedidoException ex) {
                        return false;
                    }
                }));
            }
            inicio.countDown();
            int exitosas = 0;
            for (Future<Boolean> resultado : resultados) {
                if (resultado.get(10, TimeUnit.SECONDS)) exitosas++;
            }
            assertEquals(1, exitosas);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void httpAplicaPaseValidacionesYErroresUniformes() throws Exception {
        assertEquals(200, enviar("GET", "/api/salas", null, false).statusCode());
        assertEquals(200, enviar("GET", "/public/cartelera", null, false).statusCode());
        var sinPase = enviar("POST", "/api/salas", "{}", false);
        assertEquals(401, sinPase.statusCode());
        assertTrue(sinPase.body().contains("PASE_INVALIDO"));
        var invalida = enviar("POST", "/api/bandas", """
                {"codigo":"ROCK-1234","nombre":"Banda","generoMusical":"Rock",
                "correoRepresentante":"banda@example.com","numeroIntegrantes":4}
                """, true);
        assertEquals(400, invalida.statusCode());
        assertTrue(invalida.body().contains("VALIDACION_FALLIDA"));
        assertTrue(invalida.body().contains("codigo"));
        assertEquals(404, enviar("GET", "/api/reservas/" + UUID.randomUUID(), null, true).statusCode());
        assertEquals(400, enviar("GET", "/api/reservas/no-es-uuid", null, true).statusCode());
    }

    @Test
    void respuestasDeReservaUsanDtoYConservanElContratoHttp() throws Exception {
        Banda banda = banda();
        Sala sala = sala();
        LocalDate fecha = LocalDate.now(clock).plusDays(1);
        String cuerpo = """
                {"bandaId":"%s","salaId":"%s","fecha":"%s",
                 "horaInicio":"10:00:00","duracionHoras":4}
                """.formatted(banda.id(), sala.id(), fecha);

        // Prueba HTTP real: valida estado, Location y todos los campos públicos.
        var creada = enviar("POST", "/api/reservas", cuerpo, true);
        assertEquals(201, creada.statusCode());
        JsonMapper mapper = new JsonMapper();
        JsonNode json = mapper.readTree(creada.body());
        UUID id = UUID.fromString(json.get("id").asText());
        String ruta = "/api/reservas/" + id;
        assertEquals(ruta, creada.headers().firstValue("Location").orElseThrow());
        comprobarContrato(json, id, banda.id(), sala.id(), fecha, "CONFIRMADA");
        assertInstanceOf(ReservaResponse.class, reservaController.buscar(id));

        var consulta = enviar("GET", ruta, null, true);
        assertEquals(200, consulta.statusCode());
        assertEquals(json, mapper.readTree(consulta.body()));

        var listado = enviar("GET", "/api/bandas/" + banda.id() + "/reservas?fecha=" + fecha, null, true);
        assertEquals(200, listado.statusCode());
        JsonNode elementos = mapper.readTree(listado.body());
        assertEquals(1, elementos.size());
        assertEquals(json, elementos.get(0));

        var cancelada = enviar("PATCH", ruta + "/cancelacion", null, true);
        assertEquals(200, cancelada.statusCode());
        JsonNode cancelacion = mapper.readTree(cancelada.body());
        comprobarContrato(cancelacion, id, banda.id(), sala.id(), fecha, "CANCELADA");
        assertEquals(json.get("creadaEn"), cancelacion.get("creadaEn"));

        // Un fallo no debe convertirse en una respuesta de éxito por el mapeo.
        var inexistente = enviar("GET", "/api/reservas/" + UUID.randomUUID(), null, true);
        assertEquals(404, inexistente.statusCode());
        assertEquals("RECURSO_NO_ENCONTRADO", mapper.readTree(inexistente.body()).get("codigo").asText());
    }

    private void comprobarContrato(JsonNode json, UUID id, UUID bandaId, UUID salaId,
                                    LocalDate fecha, String estado) {
        Set<String> campos = new HashSet<>();
        json.propertyNames().forEach(campos::add);
        assertEquals(Set.of("id", "bandaId", "salaId", "fecha", "horaInicio",
                "duracionHoras", "total", "estado", "creadaEn"), campos);
        assertEquals(id.toString(), json.get("id").asText());
        assertEquals(bandaId.toString(), json.get("bandaId").asText());
        assertEquals(salaId.toString(), json.get("salaId").asText());
        assertEquals(fecha.toString(), json.get("fecha").asText());
        assertEquals(LocalTime.of(10, 0), LocalTime.parse(json.get("horaInicio").asText()));
        assertEquals(4, json.get("duracionHoras").asInt());
        assertEquals(0, new BigDecimal("50.00").compareTo(new BigDecimal(json.get("total").asText())));
        assertEquals(estado, json.get("estado").asText());
        assertDoesNotThrow(() -> Instant.parse(json.get("creadaEn").asText()));
    }

    @Test
    void swaggerDocumentaElPaseYConservaConsultasPublicas() throws Exception {
        var respuesta = enviar("GET", "/v3/api-docs", null, false);
        assertEquals(200, respuesta.statusCode());
        JsonNode documento = new JsonMapper().readTree(respuesta.body());
        var esquema = documento.get("components").get("securitySchemes").get("BackstagePass");
        assertEquals("apiKey", esquema.get("type").asText());
        assertEquals("header", esquema.get("in").asText());
        assertEquals("X-Backstage-Pass", esquema.get("name").asText());
        var rutas = documento.get("paths");
        assertTrue(rutas.get("/api/reservas").get("post").get("security").get(0).has("BackstagePass"));
        assertTrue(rutas.get("/api/salas").get("post").get("security").get(0).has("BackstagePass"));
        assertFalse(rutas.get("/api/salas").get("get").has("security"));
        assertFalse(rutas.get("/public/cartelera").get("get").has("security"));
        var interfaz = enviar("GET", "/swagger-ui/index.html", null, false);
        assertEquals(200, interfaz.statusCode());
        assertTrue(interfaz.body().contains("Swagger UI"));
    }

    private HttpResponse<String> enviar(String metodo, String ruta, String cuerpo, boolean pase)
            throws Exception {
        String base = "http://localhost:" + environment.getProperty("local.server.port");
        var request = HttpRequest.newBuilder(URI.create(base + ruta))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(cuerpo));
        if (pase) request.header("X-Backstage-Pass", environment.getProperty("backstage.pase"));
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
