package org.example.actividad.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CrearSalaRequest(
        @NotBlank @Size(max = 80) String nombre,
        @NotNull @Min(1) @Max(50) Integer capacidad,
        @NotNull @DecimalMin(value = "0.01", message = "¡La tarifa debe ser mayor que cero!")
        @Digits(integer = 6, fraction = 2) BigDecimal tarifaHora,
        @NotBlank @Size(max = 500) String descripcionEquipo) {}
