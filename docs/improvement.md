# Focused improvement: reservation response DTO

## Existing issue

`ReservaController` returned the `Reserva` domain record directly. The band reservation
listing in `BandaController` also exposed `List<Reserva>`. Adding a component to that
record could unintentionally change the public JSON representation.

## Before

```java
public ResponseEntity<Reserva> crear(@Valid @RequestBody CrearReservaRequest solicitud) {
    Reserva reserva = reservas.crear(solicitud);
    return ResponseEntity.created(URI.create("/api/reservas/" + reserva.id())).body(reserva);
}
```

## After

```java
public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody CrearReservaRequest solicitud) {
    Reserva reserva = reservas.crear(solicitud);
    return ResponseEntity.created(URI.create("/api/reservas/" + reserva.id()))
            .body(ReservaResponse.desde(reserva));
}
```

`ReservaResponse` explicitly maps the original nine fields. Mapping takes place at
the HTTP boundary. The service and repository continue to work with `Reserva`.
Create, retrieve, cancel and list-by-band responses all use this DTO.

## Preserved behavior

- The original JSON field names and values remain available.
- Creation returns 201 and a Location header; other successful operations return 200.
- Error responses still use ErrorBackstage and RestControllerAdvice.
- A band may reserve at most six confirmed hours per date, across all rooms.
- Cancelled reservations release the daily quota.
- IoC/DI, two interceptors, three aspects and Bean Validation remain implemented.
- In-memory storage and the existing package structure are preserved.

## Verification

Command used for the baseline:

```powershell
.\mvnw.cmd "-Dmaven.repo.local=$env:USERPROFILE/.m2/repository" test
```

The baseline has six tests, zero failures and zero errors. The original summaries
and Maven result are saved in `evidence/baseline-tests.txt`.

Final verification uses the same suite with cached dependencies (`-o`):

```powershell
.\mvnw.cmd "-Dmaven.repo.local=$env:USERPROFILE/.m2/repository" -o test
```

The additional test `respuestasDeReservaUsanDtoYConservanElContratoHttp` exercises
creation, retrieval, listing and cancellation through a real HTTP server. It checks
all public fields, monetary total, timestamps, status codes and Location. It also
checks the response DTO type and the existing not-found error contract.

Final result: **seven tests, zero failures, zero errors, zero skipped; BUILD SUCCESS**.
The Surefire summaries and Maven result are saved in `evidence/final-tests.txt`.
Compared with the baseline, one test was added and all six existing tests still pass.

## Risk and rollback

An omitted or incorrectly mapped field could break clients. The integration test
checks the exact field set and expected values across all four operations.

To revert, restore the controllers' original domain return types and remove their
DTO conversions. Remove ReservaResponse and its dedicated test. Run the complete
baseline suite again. No stored-data migration is necessary.

## Authorship disclosure

The AI assistant generated the DTO, controller changes, regression test and this
technical note at the student's request. Automated test execution is recorded in
the evidence files. Student review, explanation and authorship statements must be
provided by the student; this note does not claim those activities have occurred.
