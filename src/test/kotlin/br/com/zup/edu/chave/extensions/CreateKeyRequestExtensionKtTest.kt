package br.com.zup.edu.chave.extensions

import br.com.zup.edu.*
import br.com.zup.edu.chave.ChavePixRepository
import br.com.zup.edu.chave.bcb.BcbClient
import br.com.zup.edu.chave.cliente.*
import br.com.zup.edu.chave.exceptions.PixClientNotFoundException
import br.com.zup.edu.chave.exceptions.PixPermissionDeniedException
import io.grpc.Status
import io.grpc.StatusRuntimeException
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

@MicronautTest(transactional = false, rollback = false)
internal class CreateKeyRequestExtensionKtTest {
    @Inject lateinit var mockChaveClient: ChaveClient
    @Inject lateinit var client: KeymanagerGRPCServiceGrpc.KeymanagerGRPCServiceBlockingStub
    @Inject lateinit var repository: ChavePixRepository
    @Inject lateinit var buscaCliente: BuscaClienteDetalhes

    private val DEFAULT_NUMERO = UUID.randomUUID().toString()
    private val DEFAULT_TIPO_CONTA = TipoConta.CONTA_CORRENTE
    private val DEFAULT_TIPO_CHAVE = TipoChave.CPF
    private val DEFAULT_CHAVE = "12345678901"

    private val request = CreateKeyRequest.newBuilder()
        .setChave(DEFAULT_CHAVE)
        .setTipoChave(DEFAULT_TIPO_CHAVE)
        .setIdCliente(DEFAULT_NUMERO)
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
            val detalhes = buscaCliente.buscaDetalhesCliente(request.idCliente, request.tipoConta)
            request.validaDadosClientes(detalhes)
        }
    }

    @Test
    fun `Testa cliente nao encontrado no ERP Http Exception`() {
        Mockito.`when`(mockChaveClient.buscaDetalhes(Mockito.anyString(), MockitoHelper.anyObject()))
            .thenThrow(HttpClientResponseException::class.java)

        assertThrows(PixClientNotFoundException::class.java) {
            val detalhes = buscaCliente.buscaDetalhesCliente(request.idCliente, request.tipoConta)
            request.validaDadosClientes(detalhes)
        }
    }

    @Test
    fun `Testa busca cliente CPF invalido`() {
        val titular = ClienteDetalhesTitular(UUID.randomUUID().toString(), "NOME", UUID.randomUUID().toString())
        val instituicao = ClienteDetalhesInstituicao("NOME", "ISPB")
        val detalhes = ClienteDetalhes(TipoConta.CONTA_CORRENTE, instituicao, "AGENCIA", "NUMERO", titular)

        Mockito.`when`(mockChaveClient.buscaDetalhes(Mockito.anyString(), MockitoHelper.anyObject()))
            .thenReturn(detalhes)

        assertThrows(PixPermissionDeniedException::class.java) {
            val result = buscaCliente.buscaDetalhesCliente(request.idCliente, request.tipoConta)
            request.validaDadosClientes(result)
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
        val titular = ClienteDetalhesTitular(UUID.randomUUID().toString(), "NOME", UUID.randomUUID().toString())
        val instituicao = ClienteDetalhesInstituicao("NOME", "ISPB")
        val detalhes = ClienteDetalhes(TipoConta.CONTA_CORRENTE, instituicao, "AGENCIA", "NUMERO", titular)

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

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }
}