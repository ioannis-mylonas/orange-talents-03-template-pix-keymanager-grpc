package br.com.zup.edu

import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import javax.inject.Singleton

@Factory
class GrpcStub {
    @Singleton
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerGRPCServiceGrpc.KeymanagerGRPCServiceBlockingStub {
        return KeymanagerGRPCServiceGrpc.newBlockingStub(channel)
    }
}