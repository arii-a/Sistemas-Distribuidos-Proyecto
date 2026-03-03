package edu.upb.tickmaster.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CompraClient {

    private final CompraServiceGrpc.CompraServiceBlockingStub stub;

    public CompraClient(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
        this.stub = CompraServiceGrpc.newBlockingStub(channel);
    }

    public CompraResponse comprar(String usuario, int cantidad) {
        CompraRequest request = CompraRequest.newBuilder()
                .setUsuario(usuario)
                .setCantidad(cantidad)
                .build();

        return stub.comprarTicket(request);
    }
}