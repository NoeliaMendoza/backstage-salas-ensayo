package org.example.actividad.controller;

import jakarta.validation.Valid;
import org.example.actividad.dto.CrearBandaRequest;
import org.example.actividad.dto.ReservaResponse;
import org.example.actividad.model.*;
import org.example.actividad.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/bandas")
public class BandaController {
    private final BandaService bandas;
    private final ReservaService reservas;

    public BandaController(BandaService bandas, ReservaService reservas) {
        this.bandas = bandas;
        this.reservas = reservas;
    }

    @PostMapping
    public ResponseEntity<Banda> crear(@Valid @RequestBody CrearBandaRequest solicitud) {
        Banda banda = bandas.crear(solicitud);
        return ResponseEntity.created(URI.create("/api/bandas/" + banda.id())).body(banda);
    }

    @GetMapping("/{id}")
    public Banda buscar(@PathVariable UUID id) { return bandas.buscar(id); }

    @GetMapping("/{id}/reservas")
    public List<ReservaResponse> reservas(@PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return reservas.porBandaYFecha(id, fecha).stream().map(ReservaResponse::desde).toList();
    }
}
