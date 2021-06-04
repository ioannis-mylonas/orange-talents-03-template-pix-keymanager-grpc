package br.com.zup.edu.chave

import br.com.zup.edu.*
import br.com.zup.edu.chave.bcb.BcbClient
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.cliente.ClienteDetalhesTitular
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

@MicronautTest(transactional = false, rollback = false)
internal class TestaExclusaoChavePix {
    @Inject lateinit var rpc: KeymanagerGRPCServiceGrpc.KeymanagerGRPCServiceBlockingStub
    @Inject lateinit var repository: ChavePixRepository
    @Inject lateinit var client: ChaveClient
    @Inject lateinit var bcbClient: BcbClient

    private val DEFAULT_NUMERO = UUID.randomUUID().toString()

    @AfterEach
    fun teardown() {
        repository.deleteAll()
    }

    @Test
    fun `testa exclui chave pix existente`() {
        val titular = ClienteDetalhesTitular(DEFAULT_NUMERO, "NOME", "12345678901")
        Mockito.`when`(client.buscaCliente(Mockito.anyString())).thenReturn(titular)

        var chave = ChavePix(TipoChave.RANDOM,
            UUID.randomUUID().toString(),
            TipoConta.CONTA_CORRENTE, titular.id, LocalDateTime.now())

        chave = repository.save(chave)
        assertTrue(repository.count() == 1L)

        val request = DeleteKeyRequest.newBuilder()
            .setIdPix(chave.id)
            .setIdCliente(titular.id)
            .build()

        rpc.delete(request)
        assertTrue(repository.count() == 0L)
    }

    @Test
    fun `testa chave inexistente BCB`() {
        val titular = ClienteDetalhesTitular(DEFAULT_NUMERO, "NOME", "12345678901")
        Mockito.`when`(client.buscaCliente(Mockito.anyString())).thenReturn(titular)

        var chave = ChavePix(TipoChave.RANDOM,
            UUID.randomUUID().toString(),
            TipoConta.CONTA_CORRENTE, titular.id, LocalDateTime.now())

        chave = repository.save(chave)
        assertTrue(repository.count() == 1L)

        val request = DeleteKeyRequest.newBuilder()
            .setIdPix(chave.id)
            .setIdCliente(titular.id)
            .build()

        Mockito.`when`(bcbClient.deleta(Mockito.anyString(), MockitoHelper.anyObject()))
            .thenThrow(HttpClientResponseException("", HttpResponse.notFound<Any>()))

        val erro = assertThrows(StatusRuntimeException::class.java) {
            rpc.delete(request)
        }

        assertEquals(Status.NOT_FOUND.code, erro.status.code)
    }

    @Test
    fun `testa exception sem permissao para excluir chave`() {
        val titular = ClienteDetalhesTitular(DEFAULT_NUMERO, "NOME", "12345678901")
        Mockito.`when`(client.buscaCliente(Mockito.anyString())).thenReturn(titular)

        var chave = ChavePix(TipoChave.RANDOM,
            UUID.randomUUID().toString(),
            TipoConta.CONTA_CORRENTE, UUID.randomUUID().toString(), LocalDateTime.now())

        chave = repository.save(chave)
        assertTrue(repository.count() == 1L)

        val request = DeleteKeyRequest.newBuilder()
            .setIdPix(chave.id)
            .setIdCliente(titular.id)
            .build()

        val erro = assertThrows(StatusRuntimeException::class.java) {
            rpc.delete(request)
        }

        assertEquals(Status.PERMISSION_DENIED.code, erro.status.code)
    }

    @Test
    fun `testa exception sem permissao para excluir chave BCB`() {
        val cpf = UUID.randomUUID().toString()
        val titular = ClienteDetalhesTitular(DEFAULT_NUMERO, "NOME", cpf)
        Mockito.`when`(client.buscaCliente(Mockito.anyString())).thenReturn(titular)

        var chave = ChavePix(TipoChave.RANDOM,
            UUID.randomUUID().toString(),
            TipoConta.CONTA_CORRENTE, cpf, LocalDateTime.now())

        chave = repository.save(chave)
        assertTrue(repository.count() == 1L)

        Mockito.`when`(bcbClient.deleta(Mockito.anyString(), MockitoHelper.anyObject()))
            .thenThrow(HttpClientResponseException("", HttpResponse.status<Any>(HttpStatus.FORBIDDEN)))

        val request = DeleteKeyRequest.newBuilder()
            .setIdPix(chave.id)
            .setIdCliente(titular.id)
            .build()

        val erro = assertThrows(StatusRuntimeException::class.java) {
            rpc.delete(request)
        }

        assertEquals(Status.PERMISSION_DENIED.code, erro.status.code)
    }

    @Test
    fun `testa exception chave inexistente`() {
        val titular = ClienteDetalhesTitular(DEFAULT_NUMERO, "NOME", "12345678901")
        Mockito.`when`(client.buscaCliente(Mockito.anyString())).thenReturn(titular)

        assertTrue(repository.count() == 0L)

        val request = DeleteKeyRequest.newBuilder()
            .setIdPix(Random.nextLong())
            .setIdCliente(titular.id)
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

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }
}