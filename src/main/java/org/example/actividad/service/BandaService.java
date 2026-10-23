package org.example.actividad.service;

import org.example.actividad.dto.CrearBandaRequest;
import org.example.actividad.exception.*;
import org.example.actividad.model.Banda;
import org.example.actividad.repository.BandaRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class BandaService {
    private final BandaRepository bandas;
    public BandaService(BandaRepository bandas) { this.bandas = bandas; }

    public Banda crear(CrearBandaRequest solicitud) {
        // La comprobación y el alta del código deben ser atómicas.
        synchronized (bandas) {
            if (bandas.existeCodigo(solicitud.codigo())) {
                throw new ConflictoReservaException("¡Ese código ya tiene banda!");
            }
            return bandas.guardar(new Banda(UUID.randomUUID(), solicitud.codigo(), solicitud.nombre(),
                    solicitud.generoMusical(), solicitud.correoRepresentante(), solicitud.numeroIntegrantes(), true));
        }
    }

    public Banda buscar(UUID id) {
        return bandas.buscar(id).orElseThrow(() ->
                new RecursoNoEncontradoException("No encontramos a esa banda en el cartel."));
    }
}
