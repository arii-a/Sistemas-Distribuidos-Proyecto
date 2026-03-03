package edu.upb.tickmaster.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.62.2)",
    comments = "Source: compra.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class CompraServiceGrpc {

  private CompraServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "tickmaster.CompraService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<edu.upb.tickmaster.grpc.CompraRequest,
      edu.upb.tickmaster.grpc.CompraResponse> getComprarTicketMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "comprarTicket",
      requestType = edu.upb.tickmaster.grpc.CompraRequest.class,
      responseType = edu.upb.tickmaster.grpc.CompraResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<edu.upb.tickmaster.grpc.CompraRequest,
      edu.upb.tickmaster.grpc.CompraResponse> getComprarTicketMethod() {
    io.grpc.MethodDescriptor<edu.upb.tickmaster.grpc.CompraRequest, edu.upb.tickmaster.grpc.CompraResponse> getComprarTicketMethod;
    if ((getComprarTicketMethod = CompraServiceGrpc.getComprarTicketMethod) == null) {
      synchronized (CompraServiceGrpc.class) {
        if ((getComprarTicketMethod = CompraServiceGrpc.getComprarTicketMethod) == null) {
          CompraServiceGrpc.getComprarTicketMethod = getComprarTicketMethod =
              io.grpc.MethodDescriptor.<edu.upb.tickmaster.grpc.CompraRequest, edu.upb.tickmaster.grpc.CompraResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "comprarTicket"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  edu.upb.tickmaster.grpc.CompraRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  edu.upb.tickmaster.grpc.CompraResponse.getDefaultInstance()))
              .setSchemaDescriptor(new CompraServiceMethodDescriptorSupplier("comprarTicket"))
              .build();
        }
      }
    }
    return getComprarTicketMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static CompraServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CompraServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CompraServiceStub>() {
        @java.lang.Override
        public CompraServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CompraServiceStub(channel, callOptions);
        }
      };
    return CompraServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static CompraServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CompraServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CompraServiceBlockingStub>() {
        @java.lang.Override
        public CompraServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CompraServiceBlockingStub(channel, callOptions);
        }
      };
    return CompraServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static CompraServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CompraServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CompraServiceFutureStub>() {
        @java.lang.Override
        public CompraServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CompraServiceFutureStub(channel, callOptions);
        }
      };
    return CompraServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void comprarTicket(edu.upb.tickmaster.grpc.CompraRequest request,
        io.grpc.stub.StreamObserver<edu.upb.tickmaster.grpc.CompraResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getComprarTicketMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service CompraService.
   */
  public static abstract class CompraServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return CompraServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service CompraService.
   */
  public static final class CompraServiceStub
      extends io.grpc.stub.AbstractAsyncStub<CompraServiceStub> {
    private CompraServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CompraServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CompraServiceStub(channel, callOptions);
    }

    /**
     */
    public void comprarTicket(edu.upb.tickmaster.grpc.CompraRequest request,
        io.grpc.stub.StreamObserver<edu.upb.tickmaster.grpc.CompraResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getComprarTicketMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service CompraService.
   */
  public static final class CompraServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<CompraServiceBlockingStub> {
    private CompraServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CompraServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CompraServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public edu.upb.tickmaster.grpc.CompraResponse comprarTicket(edu.upb.tickmaster.grpc.CompraRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getComprarTicketMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service CompraService.
   */
  public static final class CompraServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<CompraServiceFutureStub> {
    private CompraServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CompraServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CompraServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<edu.upb.tickmaster.grpc.CompraResponse> comprarTicket(
        edu.upb.tickmaster.grpc.CompraRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getComprarTicketMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_COMPRAR_TICKET = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_COMPRAR_TICKET:
          serviceImpl.comprarTicket((edu.upb.tickmaster.grpc.CompraRequest) request,
              (io.grpc.stub.StreamObserver<edu.upb.tickmaster.grpc.CompraResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getComprarTicketMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              edu.upb.tickmaster.grpc.CompraRequest,
              edu.upb.tickmaster.grpc.CompraResponse>(
                service, METHODID_COMPRAR_TICKET)))
        .build();
  }

  private static abstract class CompraServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    CompraServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return edu.upb.tickmaster.grpc.Compra.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("CompraService");
    }
  }

  private static final class CompraServiceFileDescriptorSupplier
      extends CompraServiceBaseDescriptorSupplier {
    CompraServiceFileDescriptorSupplier() {}
  }

  private static final class CompraServiceMethodDescriptorSupplier
      extends CompraServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    CompraServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (CompraServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new CompraServiceFileDescriptorSupplier())
              .addMethod(getComprarTicketMethod())
              .build();
        }
      }
    }
    return result;
  }
}
