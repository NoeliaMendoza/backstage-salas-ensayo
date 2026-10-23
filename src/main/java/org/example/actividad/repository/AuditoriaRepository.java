package org.example.actividad.repository;

import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
@org.springframework.context.annotation.Profile("!h2")
public class AuditoriaRepository {
    public record Evento(String accion, Instant fecha) {}
    private final List<Evento> eventos = new CopyOnWriteArrayList<>();

    public void registrar(String accion, Instant fecha) { eventos.add(new Evento(accion, fecha)); }
    public List<Evento> listar() { return List.copyOf(eventos); }
}
