package org.example.actividad.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CodigoBandaValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CodigoBanda {
    String message() default "¡Código desafinado! Usa BAND- seguido de cuatro dígitos.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
