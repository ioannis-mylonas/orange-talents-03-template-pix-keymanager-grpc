package br.com.zup.edu.chave.extensions

import br.com.zup.edu.KeymanagerGRPCServiceGrpc
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import javax.inject.Singleton

@MicronautTest(transactional = false, rollback = false)
internal class AnyExtensionKtTest {
    private data class JsonTestClass(val nome: String = "")

    @Test
    fun `testa json da classe de teste`() {
        val value = JsonTestClass()
        val json = value.toJson()

        assertEquals("{\"nome\":\"\"}", json)
    }
}