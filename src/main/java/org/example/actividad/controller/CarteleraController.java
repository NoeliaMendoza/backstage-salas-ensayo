package org.example.actividad.controller;

import org.example.actividad.model.Sala;
import org.example.actividad.service.SalaService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/public/cartelera")
public class CarteleraController {
    public record Cartelera(String sistema, String mensaje, List<Sala> salas) {}
    private final SalaService salas;
    public CarteleraController(SalaService salas) { this.salas = salas; }

    @GetMapping
    public Cartelera consultar() {
        return new Cartelera("Backstage", "¡Reserva tu sala y que empiece el ensayo!", salas.listar());
    }
}
