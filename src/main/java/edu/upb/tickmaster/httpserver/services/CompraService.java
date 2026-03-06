package edu.upb.tickmaster.httpserver.services;

import edu.upb.tickmaster.httpserver.repositories.CompraRepository;

import java.sql.SQLException;
import java.util.UUID;

public class CompraService {
    private final CompraRepository repository = new CompraRepository();

    public int[] procesarCompra(int usuarioId, int monto, java.util.Date fecha, int nit,
                                int eventoId, int sectorId, int cantidad, String idempotencyKey) throws SQLException {

        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            idempotencyKey = UUID.randomUUID().toString();
        }
        return repository.insertFacturaYTickets(usuarioId, monto, fecha, nit, eventoId, sectorId, cantidad, idempotencyKey);
    }
}
