package br.com.zup.edu.chave

import br.com.zup.edu.*
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.cliente.ClienteDetalhesTitular
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

@MicronautTest(transactional = false, rollback = false)
internal class TestaExclusaoChavePix {
    @Inject lateinit var rpc: KeymanagerGRPCServiceGrpc.KeymanagerGRPCServiceBlockingStub
    @Inject lateinit var repository: ChavePixRepository
    @Inject lateinit var client: ChaveClient

    private val DEFAULT_NUMERO = UUID.randomUUID().toString()

    @AfterEach
    fun teardown() {
        repository.deleteAll()
    }

    @Test
    fun `testa exclui chave pix existente`() {
        val titular = ClienteDetalhesTitular(DEFAULT_NUMERO, "12345678901")
        Mockito.`when`(client.buscaCliente(Mockito.anyString())).thenReturn(titular)

        var chave = ChavePix(titular.id,
            TipoChave.ALEATORIO,
            UUID.randomUUID().toString(),
            TipoConta.CONTA_CORRENTE)

        chave = repository.save(chave)
        // Se mudar para count() == 1L o teste sempre falha no final
        assertTrue(repository.findAll().size == 1)

        val request = DeleteKeyRequest.newBuilder()
            .setId(chave.id)
            .setNumero(titular.id)
            .build()

        rpc.delete(request)
        // Se mudar para findAll().size == 0 sempre falha
        assertTrue(repository.count() == 0L)
        // TODO Consertar: O teste falha às vezes
    }

    @Test
    fun `testa exception sem permissao para excluir chave`() {
        val titular = ClienteDetalhesTitular(DEFAULT_NUMERO, "12345678901")
        Mockito.`when`(client.buscaCliente(Mockito.anyString())).thenReturn(titular)

        var chave = ChavePix(UUID.randomUUID().toString(),
            TipoChave.ALEATORIO,
            UUID.randomUUID().toString(),
            TipoConta.CONTA_CORRENTE)

        chave = repository.save(chave)
        assertTrue(repository.count() == 1L)

        val request = DeleteKeyRequest.newBuilder()
            .setId(chave.id)
            .setNumero(titular.id)
            .build()

        val erro = assertThrows(StatusRuntimeException::class.java) {
            rpc.delete(request)
        }

        assertEquals(Status.PERMISSION_DENIED.code, erro.status.code)
    }

    @Test
    fun `testa exception chave inexistente`() {
        val titular = ClienteDetalhesTitular(DEFAULT_NUMERO, "12345678901")
        Mockito.`when`(client.buscaCliente(Mockito.anyString())).thenReturn(titular)

        assertTrue(repository.count() == 0L)

        val request = DeleteKeyRequest.newBuilder()
            .setId(Random.nextLong())
            .setNumero(titular.id)
            .build()

        val erro = assertThrows(StatusRuntimeException::class.java) {
            rpc.delete(request)
        }

        assertEquals(Status.NOT_FOUND.code, erro.status.code)
    }

    @MockBean(ChaveClient::class)
    fun chaveClient(): ChaveClient {
        return Mockito.mock(ChaveClient::class.java)
    }
}