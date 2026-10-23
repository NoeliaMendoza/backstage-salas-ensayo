package org.example.actividad.service;

import org.example.actividad.dto.CrearSalaRequest;
import org.example.actividad.exception.RecursoNoEncontradoException;
import org.example.actividad.model.Sala;
import org.example.actividad.repository.SalaRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class SalaService {
    private final SalaRepository salas;
    public SalaService(SalaRepository salas) { this.salas = salas; }

    public Sala crear(CrearSalaRequest solicitud) {
        return salas.guardar(new Sala(UUID.randomUUID(), solicitud.nombre(), solicitud.capacidad(),
                solicitud.tarifaHora(), solicitud.descripcionEquipo(), true));
    }
    public List<Sala> listar() { return salas.listarHabilitadas(); }
    public Sala buscar(UUID id) {
        return salas.buscar(id).orElseThrow(() ->
                new RecursoNoEncontradoException("Esa sala no aparece en el escenario."));
    }
}
