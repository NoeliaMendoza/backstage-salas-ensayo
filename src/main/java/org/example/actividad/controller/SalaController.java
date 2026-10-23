package org.example.actividad.controller;

import jakarta.validation.Valid;
import org.example.actividad.dto.CrearSalaRequest;
import org.example.actividad.model.Sala;
import org.example.actividad.service.SalaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/salas")
public class SalaController {
    private final SalaService salas;
    public SalaController(SalaService salas) { this.salas = salas; }

    @PostMapping
    public ResponseEntity<Sala> crear(@Valid @RequestBody CrearSalaRequest solicitud) {
        Sala sala = salas.crear(solicitud);
        return ResponseEntity.created(URI.create("/api/salas/" + sala.id())).body(sala);
    }

    @GetMapping
    public List<Sala> listar() { return salas.listar(); }

    @GetMapping("/{id}")
    public Sala buscar(@PathVariable UUID id) { return salas.buscar(id); }
}
