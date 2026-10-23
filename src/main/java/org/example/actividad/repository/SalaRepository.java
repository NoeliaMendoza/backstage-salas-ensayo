package org.example.actividad.repository;

import org.example.actividad.model.Sala;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@org.springframework.context.annotation.Profile("!h2")
public class SalaRepository {
    private final Map<UUID, Sala> salas = new ConcurrentHashMap<>();

    public Optional<Sala> buscar(UUID id) { return Optional.ofNullable(salas.get(id)); }
    public List<Sala> listarHabilitadas() {
        return salas.values().stream().filter(Sala::habilitada)
                .sorted(Comparator.comparing(Sala::nombre)).toList();
    }
    public Sala guardar(Sala sala) { salas.put(sala.id(), sala); return sala; }
}
