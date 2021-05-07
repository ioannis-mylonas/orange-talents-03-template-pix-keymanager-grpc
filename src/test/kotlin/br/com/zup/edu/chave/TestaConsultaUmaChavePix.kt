package br.com.zup.edu.chave

import br.com.zup.edu.*
import br.com.zup.edu.chave.bcb.*
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.cliente.ClienteDetalhes
import br.com.zup.edu.chave.cliente.ClienteDetalhesInstituicao
import br.com.zup.edu.chave.cliente.ClienteDetalhesTitular
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Matchers.anyString
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

@MicronautTest(transactional = false, rollback = false)
internal class TestaConsultaUmaChavePix {
    @Inject lateinit var repository: ChavePixRepository
    @Inject lateinit var chaveClient: ChaveClient
    @Inject lateinit var bcbClient: BcbClient
    @Inject lateinit var rpc: KeymanagerGRPCServiceGrpc.KeymanagerGRPCServiceBlockingStub

    private val titular = ClienteDetalhesTitular("ID", "NOME", "CPF")
    private val instituicao = ClienteDetalhesInstituicao("NOME", "ISPB")
    private val detalhes = ClienteDetalhes(TipoConta.CONTA_CORRENTE,
        instituicao, "AGENCIA", "NUMERO", titular)

    private val bcbBankAccount = BcbBankAccount(detalhes.agencia,
        detalhes.numero, BcbAccountType.fromClienteDetalhes(detalhes))

    private val bcbOwner = BcbOwner(BcbOwnerType.NATURAL_PERSON, titular.nome, titular.cpf)
    private val bcbChave = BcbKeyDetails(TipoChave.RANDOM, UUID.randomUUID().toString(), bcbBankAccount, bcbOwner, LocalDateTime.now())

    @BeforeEach
    fun setup() {
        Mockito.`when`(chaveClient.buscaDetalhes(anyString(), MockitoHelper.anyObject()))
            .thenReturn(detalhes)

        Mockito.`when`(bcbClient.get(anyString())).thenReturn(bcbChave)
    }

    @AfterEach
    fun teardown() {
        repository.deleteAll()
    }

    @Test
    fun `testa encontra chave especificada valida`() {
        var chave = ChavePix(bcbChave.keyType, bcbChave.key, detalhes.tipo, titular.cpf, bcbChave.createdAt)
        chave = repository.save(chave)
        assertEquals(1L, repository.count())

        val request = GetKeyRequest.newBuilder()
            .setIdCliente(titular.id).setIdPix(chave.id).build()

        val response = rpc.get(request)

        assertEquals(chave.chave, response.chave)
        assertEquals(chave.id, response.idPix)
        assertEquals(chave.cpf, response.titular.cpf)
    }

    @Test
    fun `testa encontra chave especificada BCB`() {
        var chave = ChavePix(bcbChave.keyType, bcbChave.key, detalhes.tipo, titular.cpf, bcbChave.createdAt)
        chave = repository.save(chave)
        assertEquals(1L, repository.count())

        val request = GetKeyRequest.newBuilder()
            .setIdCliente(titular.id).setIdPix(chave.id).build()

        Mockito.`when`(bcbClient.get(anyString())).thenThrow(HttpClientResponseException("", HttpResponse.notFound<Any>()))

        val error = assertThrows(StatusRuntimeException::class.java) {
            rpc.get(request)
        }

        assertEquals(Status.NOT_FOUND.code, error.status.code)
    }

    @Test
    fun `testa nao encontra chave especificada`() {
        assertEquals(0L, repository.count())

        val request = GetKeyRequest.newBuilder()
            .setIdCliente(titular.id).setIdPix(Random.nextLong()).build()

        val erro = assertThrows(StatusRuntimeException::class.java) {
            rpc.get(request)
        }
        assertEquals(Status.NOT_FOUND.code, erro.status.code)
    }

    @Test
    fun `testa nao encontra dono`() {
        var chave = ChavePix(bcbChave.keyType, bcbChave.key, detalhes.tipo, titular.cpf, bcbChave.createdAt)
        chave = repository.save(chave)
        assertEquals(1L, repository.count())

        val request = GetKeyRequest.newBuilder()
            .setIdCliente(titular.id).setIdPix(chave.id).build()

        Mockito.`when`(chaveClient.buscaDetalhes(anyString(), MockitoHelper.anyObject()))
            .thenThrow(HttpClientResponseException("", HttpResponse.notFound<Any>()))

        val erro = assertThrows(StatusRuntimeException::class.java) {
            rpc.get(request)
        }
        assertEquals(Status.NOT_FOUND.code, erro.status.code)
    }

    @Test
    fun `testa nao encontra dono null`() {
        var chave = ChavePix(bcbChave.keyType, bcbChave.key, detalhes.tipo, titular.cpf, bcbChave.createdAt)
        chave = repository.save(chave)
        assertEquals(1L, repository.count())

        val request = GetKeyRequest.newBuilder()
            .setIdCliente(titular.id).setIdPix(chave.id).build()

        Mockito.`when`(chaveClient.buscaDetalhes(anyString(), MockitoHelper.anyObject()))
            .thenReturn(null)

        val erro = assertThrows(StatusRuntimeException::class.java) {
            rpc.get(request)
        }
        assertEquals(Status.NOT_FOUND.code, erro.status.code)
    }

    @Test
    fun `testa nao e dono da chave`() {
        val outroTitular = ClienteDetalhesTitular("ID", "NOME", UUID.randomUUID().toString())
        val outroInstituicao = ClienteDetalhesInstituicao("NOME", "ISPB")
        val outroDetalhes = ClienteDetalhes(TipoConta.CONTA_CORRENTE,
            outroInstituicao, "AGENCIA", "NUMERO", outroTitular)

        Mockito.`when`(chaveClient.buscaDetalhes(anyString(), MockitoHelper.anyObject()))
            .thenReturn(outroDetalhes)

        var chave = ChavePix(bcbChave.keyType, bcbChave.key, detalhes.tipo, titular.cpf, bcbChave.createdAt)
        chave = repository.save(chave)
        assertEquals(1L, repository.count())

        val request = GetKeyRequest.newBuilder()
            .setIdCliente(outroTitular.id).setIdPix(chave.id).build()

        val error = assertThrows(StatusRuntimeException::class.java) {
            rpc.get(request)
        }

        assertEquals(Status.PERMISSION_DENIED.code, error.status.code)
    }

    @MockBean(ChaveClient::class)
    fun chaveClient(): ChaveClient {
        return Mockito.mock(ChaveClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return Mockito.mock(BcbClient::class.java, Mockito.RETURNS_DEEP_STUBS)
    }
}