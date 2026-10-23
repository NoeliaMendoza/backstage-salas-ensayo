package org.example.actividad.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Sala(UUID id, String nombre, int capacidad, BigDecimal tarifaHora,
                   String descripcionEquipo, boolean habilitada) {}
