package edu.upb.tickmaster.grpc;

import io.grpc.stub.StreamObserver;

import edu.upb.tickmaster.db.ConnectionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CompraServiceImpl extends edu.upb.tickmaster.grpc.CompraServiceGrpc.CompraServiceImplBase {

    @Override
    public void comprarTicket(edu.upb.tickmaster.grpc.CompraRequest request,
                              StreamObserver<edu.upb.tickmaster.grpc.CompraResponse> responseObserver) {

            String usuario = request.getUsuario();
            int cantidad = request.getCantidad();

            System.out.printf("Compra recibida: usuario=%s, cantidad=%d%n",
                    usuario, cantidad);

            String compraId = "COMPRA-" + System.currentTimeMillis();

            try {
                Connection conn = ConnectionDB.instance().getConnection();

                String sql = "INSERT INTO compras (usuario, cantidad) VALUES (?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, usuario);
                stmt.setInt(2, cantidad);
                stmt.executeUpdate();

                System.out.printf("Compra guardada: usuario=%s, cantidad=%d%n", usuario, cantidad);

                CompraResponse response = CompraResponse.newBuilder()
                        .setExitoso(true)
                        .setMensaje("Compra registrada exitosamente")
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();

            } catch (SQLException e) {
                System.err.println("Error al guardar en DB: " + e.getMessage());

                CompraResponse errorResponse = CompraResponse.newBuilder()
                        .setExitoso(false)
                        .setMensaje("Error al registrar la compra: " + e.getMessage())
                        .build();

                responseObserver.onNext(errorResponse);
                responseObserver.onCompleted();
            }
    }
}