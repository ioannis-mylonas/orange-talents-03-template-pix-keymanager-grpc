package br.com.zup.edu

import br.com.zup.edu.chave.bcb.BcbClient
import br.com.zup.edu.chave.cliente.ChaveClient
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import org.mockito.Mockito
import javax.inject.Singleton

@Factory
class TestFactory {
    @Singleton
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerGRPCServiceGrpc.KeymanagerGRPCServiceBlockingStub {
        return KeymanagerGRPCServiceGrpc.newBlockingStub(channel)
    }
}