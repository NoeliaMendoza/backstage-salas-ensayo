package org.example.actividad.config;

public record PoliticaEnsayo(int maxHorasDiarias) {
    public PoliticaEnsayo {
        if (maxHorasDiarias != 6) throw new IllegalArgumentException("El límite debe ser seis horas.");
    }
}
