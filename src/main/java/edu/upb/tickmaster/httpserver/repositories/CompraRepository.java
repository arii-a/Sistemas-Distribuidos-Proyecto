package edu.upb.tickmaster.httpserver.repositories;

import edu.upb.tickmaster.db.ConnectionDB;
import edu.upb.tickmaster.httpserver.handlers.CompraHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class CompraRepository {
    private static final Logger logger = LoggerFactory.getLogger(CompraHandler.class);

    private Connection getConnection() {
        return ConnectionDB.instance().getConnection();
    }

    public int[] insertFacturaYTickets(int usuarioId, int monto, java.util.Date fecha, int nit,
                                        int eventoId, int sectorId, int cantidad,
                                        String idempotencyKey) throws SQLException {
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);

            int facturaId;

            String sqlFactura =
                    "INSERT INTO public.factura (user_id, monto, fecha, nit, idempotency_key) " +
                            "VALUES (?, ?, ?, ?, ?) RETURNING id";

            try (PreparedStatement stmtFactura = conn.prepareStatement(sqlFactura)) {
                stmtFactura.setInt(1, usuarioId);
                stmtFactura.setInt(2, monto);
                stmtFactura.setTimestamp(3, new Timestamp(fecha.getTime()));
                stmtFactura.setInt(4, nit);
                stmtFactura.setString(5, idempotencyKey);

                try (ResultSet rs = stmtFactura.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return new int[]{500, -1};
                    }
                    facturaId = rs.getInt("id");
                }
            }

            String sqlTicket =
                    "INSERT INTO public.ticket_compra (usuario_id, evento_id, sector_id, factura_id) " +
                            "VALUES (?, ?, ?, ?)";

            try (PreparedStatement stmtTicket = conn.prepareStatement(sqlTicket)) {
                for (int i = 0; i < cantidad; i++) {
                    stmtTicket.setInt(1, usuarioId);
                    stmtTicket.setInt(2, eventoId);
                    stmtTicket.setInt(3, sectorId);
                    stmtTicket.setInt(4, facturaId);
                    stmtTicket.addBatch();
                }
                stmtTicket.executeBatch();
            }

            conn.commit();
            logger.info("Factura y {} tickets creados exitosamente", cantidad);
            return new int[]{201, facturaId};

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            if ("23505".equals(e.getSQLState())) {
                logger.info("Solicitud duplicada detectada.");
                return new int[]{200, -1};
            }

            logger.warn("Error en transacción: {}", e.getMessage(), e);
            return new int[]{500, -1};

        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
