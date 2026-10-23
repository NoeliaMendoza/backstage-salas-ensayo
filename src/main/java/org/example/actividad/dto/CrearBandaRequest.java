package org.example.actividad.dto;

import jakarta.validation.constraints.*;
import org.example.actividad.validation.CodigoBanda;

public record CrearBandaRequest(
        @NotBlank(message = "¡Falta el código de la banda!") @CodigoBanda String codigo,
        @NotBlank @Size(min = 2, max = 60) String nombre,
        @NotBlank String generoMusical,
        @NotBlank @Email(message = "Ese correo no llega al camerino.") String correoRepresentante,
        @NotNull @Min(1) @Max(20) Integer numeroIntegrantes) {}
