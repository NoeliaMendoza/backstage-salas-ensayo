package org.example.actividad.controller;

import jakarta.validation.Valid;
import org.example.actividad.dto.CrearReservaRequest;
import org.example.actividad.dto.ReservaResponse;
import org.example.actividad.model.Reserva;
import org.example.actividad.service.ReservaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {
    private final ReservaService reservas;
    public ReservaController(ReservaService reservas) { this.reservas = reservas; }

    @PostMapping
    public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody CrearReservaRequest solicitud) {
        Reserva reserva = reservas.crear(solicitud);
        return ResponseEntity.created(URI.create("/api/reservas/" + reserva.id()))
                .body(ReservaResponse.desde(reserva));
    }

    @GetMapping("/{id}")
    public ReservaResponse buscar(@PathVariable UUID id) {
        return ReservaResponse.desde(reservas.buscar(id));
    }

    @PatchMapping("/{id}/cancelacion")
    public ReservaResponse cancelar(@PathVariable UUID id) {
        return ReservaResponse.desde(reservas.cancelar(id));
    }
}
