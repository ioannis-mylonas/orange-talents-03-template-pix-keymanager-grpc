package br.com.zup.edu.chave

import br.com.zup.edu.*
import br.com.zup.edu.chave.bcb.*
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.cliente.ClienteDetalhes
import br.com.zup.edu.chave.cliente.ClienteDetalhesInstituicao
import br.com.zup.edu.chave.cliente.ClienteDetalhesTitular
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Matchers.eq
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false, rollback = false)
internal class TestaListagemChavePix {
    @Inject lateinit var chaveClient: ChaveClient
    @Inject lateinit var bcbClient: BcbClient
    @Inject lateinit var rpc: KeymanagerGRPCServiceGrpc.KeymanagerGRPCServiceBlockingStub
    @Inject lateinit var repository: ChavePixRepository

    private val titular1 = ClienteDetalhesTitular("1", "1", "1")
    private val instituicao1 = ClienteDetalhesInstituicao("1", "1")
    private val detalhes1 = ClienteDetalhes(TipoConta.CONTA_CORRENTE,
        instituicao1, "1", "1", titular1)
    private val chaves1 = listOf(
        ChavePix(TipoChave.RANDOM, UUID.randomUUID().toString(), detalhes1.tipo, titular1.cpf, LocalDateTime.now()),
        ChavePix(TipoChave.CPF, titular1.cpf, detalhes1.tipo, titular1.cpf, LocalDateTime.now()),
        ChavePix(TipoChave.PHONE, "+123", detalhes1.tipo, titular1.cpf, LocalDateTime.now()),
        ChavePix(TipoChave.EMAIL, "1@email.com", detalhes1.tipo, titular1.cpf, LocalDateTime.now())
    )

    private val titular2 = ClienteDetalhesTitular("2", "2", "2")
    private val instituicao2 = ClienteDetalhesInstituicao("2", "2")
    private val detalhes2 = ClienteDetalhes(TipoConta.CONTA_POUPANCA,
        instituicao2, "2", "2", titular2)
    private val chaves2 = listOf(
        ChavePix(TipoChave.RANDOM, UUID.randomUUID().toString(), detalhes2.tipo, titular2.cpf, LocalDateTime.now()),
        ChavePix(TipoChave.CPF, titular2.cpf, detalhes2.tipo, titular2.cpf, LocalDateTime.now()),
        ChavePix(TipoChave.PHONE, "+456", detalhes2.tipo, titular2.cpf, LocalDateTime.now()),
        ChavePix(TipoChave.EMAIL, "2@email.com", detalhes2.tipo, titular2.cpf, LocalDateTime.now())
    )

    @BeforeEach
    fun setup() {
        val list1 = BcbKeyDetailsList(chaves1.map {
            BcbKeyDetails(it.tipoChave, it.chave,
                BcbBankAccount(detalhes1.agencia, detalhes1.numero, BcbAccountType.fromClienteDetalhes(detalhes1)),
                BcbOwner(BcbOwnerType.NATURAL_PERSON, titular1.nome, titular1.cpf),
                LocalDateTime.now()
            )
        })

        Mockito.`when`(chaveClient.buscaCliente(MockitoHelper.eq(titular1.id))).thenReturn(titular1)
        Mockito.`when`(chaveClient.buscaDetalhes(MockitoHelper.eq(titular1.id), MockitoHelper.anyObject())).thenReturn(detalhes1)
        list1.pixKeys.forEach {
            Mockito.`when`(bcbClient.get(MockitoHelper.eq(it.key))).thenReturn(it)
        }

        val list2 = BcbKeyDetailsList(chaves2.map {
            BcbKeyDetails(it.tipoChave, it.chave,
                BcbBankAccount(detalhes2.agencia, detalhes2.numero, BcbAccountType.fromClienteDetalhes(detalhes2)),
                BcbOwner(BcbOwnerType.NATURAL_PERSON, titular2.nome, titular2.cpf),
                LocalDateTime.now()
            )
        })

        Mockito.`when`(chaveClient.buscaCliente(MockitoHelper.eq(titular2.id))).thenReturn(titular2)
        Mockito.`when`(chaveClient.buscaDetalhes(MockitoHelper.eq(titular2.id), MockitoHelper.anyObject())).thenReturn(detalhes2)
        list2.pixKeys.forEach {
            Mockito.`when`(bcbClient.get(MockitoHelper.eq(it.key))).thenReturn(it)
        }
    }

    @AfterEach
    fun teardown() {
        repository.deleteAll()
    }

    @Test
    fun `testa lista todas as chaves do usuario 1`() {
        repository.saveAll(chaves1)
        repository.saveAll(chaves2)

        val request = ListKeyRequest.newBuilder().setIdCliente(titular1.id).build()

        val response = rpc.list(request)
        assertEquals(chaves1.size, response.chavesCount)
    }

    @Test
    fun `testa exception lista usuario inexistente`() {
        repository.saveAll(chaves1)
        repository.saveAll(chaves2)

        val request = ListKeyRequest.newBuilder()
            .setIdCliente(UUID.randomUUID().toString())
            .build()

        val erro = assertThrows(StatusRuntimeException::class.java) {
            rpc.list(request)
        }

        assertEquals(Status.NOT_FOUND.code, erro.status.code)
    }

    @Test
    fun `testa nao encontra chaves nao salvas usuario 1`() {
        val request = ListKeyRequest.newBuilder().setIdCliente(titular1.id).build()

        val response = rpc.list(request)
        assertEquals(0, response.chavesCount)
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