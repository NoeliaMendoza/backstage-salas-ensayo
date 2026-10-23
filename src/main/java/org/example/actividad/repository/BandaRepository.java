package org.example.actividad.repository;

import org.example.actividad.model.Banda;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@org.springframework.context.annotation.Profile("!h2")
public class BandaRepository {
    private final Map<UUID, Banda> bandas = new ConcurrentHashMap<>();

    public Optional<Banda> buscar(UUID id) { return Optional.ofNullable(bandas.get(id)); }
    public boolean existeCodigo(String codigo) {
        return bandas.values().stream().anyMatch(b -> b.codigo().equals(codigo));
    }
    public Banda guardar(Banda banda) { bandas.put(banda.id(), banda); return banda; }
}
