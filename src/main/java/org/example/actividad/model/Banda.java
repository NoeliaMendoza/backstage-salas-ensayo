package org.example.actividad.model;

import java.util.UUID;

public record Banda(UUID id, String codigo, String nombre, String generoMusical,
                    String correoRepresentante, int numeroIntegrantes, boolean activa) {}
