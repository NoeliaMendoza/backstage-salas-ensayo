package org.example.actividad.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CodigoBandaValidator implements ConstraintValidator<CodigoBanda, String> {
    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        // La ausencia del código corresponde a @NotBlank.
        return valor == null || valor.isBlank() || valor.matches("BAND-[0-9]{4}");
    }
}
