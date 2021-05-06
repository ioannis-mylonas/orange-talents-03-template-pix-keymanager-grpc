package br.com.zup.edu.chave.extensions

import br.com.zup.edu.*
import br.com.zup.edu.chave.ChavePixRepository
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.cliente.ClienteDetalhes
import br.com.zup.edu.chave.cliente.ClienteDetalhesTitular
import br.com.zup.edu.chave.exceptions.PixClientNotFoundException
import br.com.zup.edu.chave.exceptions.PixPermissionDeniedException
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false, rollback = false)
internal class CreateKeyRequestExtensionKtTest {
    @Inject lateinit var mockChaveClient: ChaveClient
    @Inject lateinit var client: KeymanagerGRPCServiceGrpc.KeymanagerGRPCServiceBlockingStub
    @Inject lateinit var repository: ChavePixRepository

    private val DEFAULT_NUMERO = UUID.randomUUID().toString()
    private val DEFAULT_TIPO_CONTA = TipoConta.CONTA_CORRENTE
    private val DEFAULT_TIPO_CHAVE = TipoChave.CPF
    private val DEFAULT_CHAVE = "12345678901"

    private val request = CreateKeyRequest.newBuilder()
        .setChave(DEFAULT_CHAVE)
        .setTipoChave(DEFAULT_TIPO_CHAVE)
        .setNumero(DEFAULT_NUMERO)
        .setTipoConta(DEFAULT_TIPO_CONTA)
        .build()

    @AfterEach
    fun teardown() {
        repository.deleteAll()
    }

    @Test
    fun `Testa cliente nao encontrado no ERP`() {
        Mockito.`when`(mockChaveClient.buscaDetalhes(Mockito.anyString(), MockitoHelper.anyObject()))
            .thenReturn(null)

        assertThrows(PixClientNotFoundException::class.java) {
            request.validateDadosClientes(mockChaveClient)
        }
    }

    @Test
    fun `Testa cliente nao encontrado no ERP Http Exception`() {
        Mockito.`when`(mockChaveClient.buscaDetalhes(Mockito.anyString(), MockitoHelper.anyObject()))
            .thenThrow(HttpClientResponseException::class.java)

        assertThrows(PixClientNotFoundException::class.java) {
            request.validateDadosClientes(mockChaveClient)
        }
    }

    @Test
    fun `Testa busca cliente CPF invalido`() {
        val titular = ClienteDetalhesTitular(UUID.randomUUID().toString(), UUID.randomUUID().toString())
        val detalhes = ClienteDetalhes(titular)

        Mockito.`when`(mockChaveClient.buscaDetalhes(Mockito.anyString(), MockitoHelper.anyObject()))
            .thenReturn(detalhes)

        assertThrows(PixPermissionDeniedException::class.java) {
            request.validateDadosClientes(mockChaveClient)
        }
    }

    @Test
    fun `Testa cliente inexistente por gRPC`() {
        Mockito.`when`(mockChaveClient.buscaDetalhes(Mockito.anyString(), MockitoHelper.anyObject()))
            .thenReturn(null)

        val erro = assertThrows(StatusRuntimeException::class.java) {
            client.cria(request)
        }

        assertEquals(Status.NOT_FOUND.code, erro.status.code)
    }

    @Test
    fun `Testa cliente CPF invalido por gRPC`() {
        val titular = ClienteDetalhesTitular(UUID.randomUUID().toString(), UUID.randomUUID().toString())
        val detalhes = ClienteDetalhes(titular)

        Mockito.`when`(mockChaveClient.buscaDetalhes(Mockito.anyString(), MockitoHelper.anyObject()))
            .thenReturn(detalhes)

        val erro = assertThrows(StatusRuntimeException::class.java) {
            client.cria(request)
        }

        assertEquals(Status.PERMISSION_DENIED.code, erro.status.code)
    }

    @MockBean(ChaveClient::class)
    fun chaveClient(): ChaveClient {
        return Mockito.mock(ChaveClient::class.java)
    }
}