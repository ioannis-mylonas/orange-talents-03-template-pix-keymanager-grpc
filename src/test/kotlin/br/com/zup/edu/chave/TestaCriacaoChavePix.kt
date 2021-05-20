package br.com.zup.edu.chave

import br.com.zup.edu.*
import br.com.zup.edu.chave.bcb.BcbAccountType
import br.com.zup.edu.chave.bcb.BcbClient
import br.com.zup.edu.chave.bcb.BcbCreatePixKeyResponse
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.cliente.ClienteDetalhes
import br.com.zup.edu.chave.cliente.ClienteDetalhesInstituicao
import br.com.zup.edu.chave.cliente.ClienteDetalhesTitular
import br.com.zup.edu.chave.extensions.asBadRequest
import br.com.zup.edu.chave.extensions.hasFieldViolations
import com.google.rpc.BadRequest
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EmptySource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false, rollback = false)
internal class TestaCriacaoChavePix {
    @Inject lateinit var client: KeymanagerGRPCServiceGrpc.KeymanagerGRPCServiceBlockingStub
    @Inject lateinit var repository: ChavePixRepository
    @Inject lateinit var mockChaveClient: ChaveClient
    @Inject lateinit var mockBcbClient: BcbClient

    private val DEFAULT_NUMERO = UUID.randomUUID().toString()
    private val DEFAULT_TIPO_CONTA = TipoConta.CONTA_CORRENTE
    private val DEFAULT_TIPO_CHAVE = TipoChave.CPF
    private val DEFAULT_CHAVE = "12345678901"

    private val requestBuilder = CreateKeyRequest.newBuilder()
        .setIdCliente(DEFAULT_NUMERO)
        .setTipoConta(DEFAULT_TIPO_CONTA)
        .setTipoChave(DEFAULT_TIPO_CHAVE)
        .setChave(DEFAULT_CHAVE)

    private val instituicao = ClienteDetalhesInstituicao("NOME", "ISPB")
    private val titular = ClienteDetalhesTitular("ID", "NOME", DEFAULT_NUMERO)
    private val detalhes = ClienteDetalhes(DEFAULT_TIPO_CONTA, instituicao, "AGENCIA", DEFAULT_NUMERO, titular)

    private val mockResponse = Mockito.mock(BcbCreatePixKeyResponse::class.java, Mockito.RETURNS_DEEP_STUBS)

    /**
     * Retorna a StatusRuntimeException jogada pelo servidor (conforme esperado no teste)
     * @param request Request a ser enviada para o servidor
     * @param code Status.Code esperado da StatusRuntimeException
     */
    private fun getException(request: CreateKeyRequest, code: Status.Code): StatusRuntimeException {
        val error = assertThrows(StatusRuntimeException::class.java) {
            client.cria(request)
        }

        assertEquals(code, error.status.code)
        return error
    }

    /**
     * Recebe um BadRequest de um StatusRuntimeException
     * @param e StatusRuntimeException jogado pelo servidor
     * @return BadRequest com os field violations e seus detalhes
     */
    private fun getBadRequest(e: StatusRuntimeException): BadRequest {
        val status = StatusProto.fromThrowable(e) ?: fail()
        return status.detailsList[0].asBadRequest()
    }

    @BeforeEach
    fun setup() {
        Mockito.`when`(mockChaveClient.buscaDetalhes(Mockito.anyString(), MockitoHelper.anyObject()))
            .thenReturn(detalhes)

        Mockito.`when`(mockBcbClient.cadastra(MockitoHelper.anyObject())).thenReturn(mockResponse)
        Mockito.`when`(mockResponse.owner.taxIdNumber).thenReturn(DEFAULT_NUMERO)
        Mockito.`when`(mockResponse.bankAccount.accountType).thenReturn(BcbAccountType.CACC)
        Mockito.`when`(mockResponse.createdAt).thenReturn(LocalDateTime.now())
    }

    @AfterEach
    fun teardown() {
        repository.deleteAll()
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = ["1", "123", "123.456.789-30", "123456789-30"])
    fun `testa requisicao CPF pattern invalida`(cpf: String) {
        val instituicao = ClienteDetalhesInstituicao("NOME", "ISPB")
        val titular = ClienteDetalhesTitular("ID", "NOME", cpf)
        val detalhes = ClienteDetalhes(DEFAULT_TIPO_CONTA, instituicao, "AGENCIA", DEFAULT_NUMERO, titular)

        Mockito.`when`(mockChaveClient.buscaDetalhes(Mockito.anyString(), MockitoHelper.anyObject()))
            .thenReturn(detalhes)
        Mockito.`when`(mockResponse.key).thenReturn(cpf)
        Mockito.`when`(mockResponse.keyType).thenReturn(TipoChave.CPF)

        val request = requestBuilder
            .setTipoChave(TipoChave.CPF)
            .setChave(cpf)
            .build()

        val e = getException(request, Status.INVALID_ARGUMENT.code)
        val badRequest = getBadRequest(e)

        val expected = mutableSetOf("chave" to "Formato de CPF inválido")
        if (cpf.isBlank()) expected.add("chave" to "must not be blank")

        assertTrue(badRequest.hasFieldViolations(expected))
    }

    @ParameterizedTest
    @ValueSource(strings = ["12345678901", "01234567890", "10320064042", "42423029080"])
    fun `testa requisicao CPF valido`(cpf: String) {
        val instituicao = ClienteDetalhesInstituicao("NOME", "ISPB")
        val titular = ClienteDetalhesTitular("ID", "NOME", cpf)
        val detalhes = ClienteDetalhes(DEFAULT_TIPO_CONTA, instituicao, "AGENCIA", DEFAULT_NUMERO, titular)

        Mockito.`when`(mockChaveClient.buscaDetalhes(Mockito.anyString(), MockitoHelper.anyObject()))
            .thenReturn(detalhes)
        Mockito.`when`(mockResponse.key).thenReturn(cpf)
        Mockito.`when`(mockResponse.keyType).thenReturn(TipoChave.CPF)

        val request = requestBuilder
            .setTipoChave(TipoChave.CPF)
            .setChave(cpf)
            .build()

        client.cria(request)
    }

    @ParameterizedTest
    @ValueSource(strings = ["12345678901", "01234567890", "10320064042", "42423029080"])
    fun `testa requisicao CPF ja existente`(cpf: String) {
        val instituicao = ClienteDetalhesInstituicao("NOME", "ISPB")
        val titular = ClienteDetalhesTitular("ID", "NOME", cpf)
        val detalhes = ClienteDetalhes(DEFAULT_TIPO_CONTA, instituicao, "AGENCIA", DEFAULT_NUMERO, titular)

        Mockito.`when`(mockChaveClient.buscaDetalhes(Mockito.anyString(), MockitoHelper.anyObject()))
            .thenReturn(detalhes)
        Mockito.`when`(mockResponse.key).thenReturn(cpf)
        Mockito.`when`(mockResponse.keyType).thenReturn(TipoChave.CPF)

        val request = requestBuilder
            .setTipoChave(TipoChave.CPF)
            .setChave(cpf)
            .build()

        client.cria(request)

        getException(request, Status.ALREADY_EXISTS.code)
    }

    @ParameterizedTest
    @ValueSource(strings = ["12345678901", "01234567890", "10320064042", "42423029080"])
    fun `testa requisicao CPF ja existente BCB`(cpf: String) {
        val instituicao = ClienteDetalhesInstituicao("NOME", "ISPB")
        val titular = ClienteDetalhesTitular("ID", "NOME", cpf)
        val detalhes = ClienteDetalhes(DEFAULT_TIPO_CONTA, instituicao, "AGENCIA", DEFAULT_NUMERO, titular)

        Mockito.`when`(mockChaveClient.buscaDetalhes(Mockito.anyString(), MockitoHelper.anyObject()))
            .thenReturn(detalhes)
        Mockito.`when`(mockBcbClient.cadastra(MockitoHelper.anyObject()).key).thenReturn(cpf)

        val request = requestBuilder
            .setTipoChave(TipoChave.CPF)
            .setChave(cpf)
            .build()

        Mockito.`when`(mockBcbClient.cadastra(MockitoHelper.anyObject()))
            .thenThrow(HttpClientResponseException("", HttpResponse.unprocessableEntity<Any>()))

        getException(request, Status.ALREADY_EXISTS.code)
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = ["emailinvalido", "naomail@", "@gmail.com.br", "eeeemail@ gmail.com"])
    fun `testa requisicao email invalido`(email: String) {
        val request = requestBuilder
            .setTipoChave(TipoChave.EMAIL)
            .setChave(email)
            .build()

        Mockito.`when`(mockResponse.key).thenReturn(email)
        Mockito.`when`(mockResponse.keyType).thenReturn(TipoChave.EMAIL)

        val e = getException(request, Status.INVALID_ARGUMENT.code)
        val badRequest = getBadRequest(e)

        val expected = mutableSetOf("chave" to "must be a well-formed email address")
        if (email.isBlank()) expected.add("chave" to "must not be blank")

        assertTrue(badRequest.hasFieldViolations(expected))
    }

    @ParameterizedTest
    @ValueSource(strings = ["joao@email.com", "maria@gmail.com", "eduardo@zup.com.br", "joe@mail"])
    fun `testa requisicao email valido`(email: String) {
        val request = requestBuilder
            .setTipoChave(TipoChave.EMAIL)
            .setChave(email)
            .build()

        Mockito.`when`(mockResponse.key).thenReturn(email)
        Mockito.`when`(mockResponse.keyType).thenReturn(TipoChave.EMAIL)
        client.cria(request)
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = ["1", "123", "+12", "+12345678912345678"])
    fun `testa requisicao celular invalido`(celular: String) {
        val request = requestBuilder
            .setTipoChave(TipoChave.PHONE)
            .setChave(celular)
            .build()

        Mockito.`when`(mockResponse.key).thenReturn(celular)
        Mockito.`when`(mockResponse.keyType).thenReturn(TipoChave.PHONE)

        val e = getException(request, Status.INVALID_ARGUMENT.code)
        val badRequest = getBadRequest(e)

        val expected = mutableSetOf("chave" to "Formato de celular inválido.")
        if (celular.isBlank()) expected.add("chave" to "must not be blank")

        assertTrue(badRequest.hasFieldViolations(expected))
    }

    @ParameterizedTest
    @ValueSource(strings = ["+123", "+12345678912345", "+5585988714077"])
    fun `testa requisicao celular valido`(celular: String) {
        val request = requestBuilder
            .setTipoChave(TipoChave.PHONE)
            .setChave(celular)
            .build()

        Mockito.`when`(mockResponse.key).thenReturn(celular)
        Mockito.`when`(mockResponse.keyType).thenReturn(TipoChave.PHONE)
        client.cria(request)
    }

    @ParameterizedTest
    @ValueSource(strings = ["+123", "+12345678912345", "+5585988714077"])
    fun `testa requisicao celular ja existente`(celular: String) {
        val request = requestBuilder
            .setTipoChave(TipoChave.PHONE)
            .setChave(celular)
            .build()

        Mockito.`when`(mockResponse.key).thenReturn(celular)
        Mockito.`when`(mockResponse.keyType).thenReturn(TipoChave.PHONE)
        client.cria(request)

        getException(request, Status.ALREADY_EXISTS.code)
    }

    @ParameterizedTest
    @ValueSource(strings = ["a", "+5585988714077", "joao@email.com", "42423029080"])
    fun `testa requisicao aleatorio string invalido`(aleatorio: String) {
        val request = requestBuilder
            .setTipoChave(TipoChave.RANDOM)
            .setChave(aleatorio)
            .build()

        Mockito.`when`(mockResponse.key).thenReturn(aleatorio)
        Mockito.`when`(mockResponse.keyType).thenReturn(TipoChave.RANDOM)

        val exception = getException(request, Status.INVALID_ARGUMENT.code)
        val badRequest = getBadRequest(exception)
        val expected = setOf("chave" to "Valor deve estar vazio.")

        assertTrue(badRequest.hasFieldViolations(expected))
    }

    @ParameterizedTest
    @EmptySource
    fun `testa requisicao aleatorio valido`(aleatorio: String) {
        val request = requestBuilder
            .setTipoChave(TipoChave.RANDOM)
            .setChave(aleatorio)
            .build()

        Mockito.`when`(mockResponse.key).thenReturn(UUID.randomUUID().toString())
        Mockito.`when`(mockResponse.keyType).thenReturn(TipoChave.RANDOM)
        client.cria(request)
    }

    @ParameterizedTest
    @EmptySource
    fun `testa requisicao aleatorio invalido BCB`(aleatorio: String) {
        val request = requestBuilder
            .setTipoChave(TipoChave.RANDOM)
            .setChave(aleatorio)
            .build()

        Mockito.`when`(mockBcbClient.cadastra(MockitoHelper.anyObject()))
            .thenThrow(HttpClientResponseException("", HttpResponse.badRequest<Any>()))

        val erro = assertThrows(StatusRuntimeException::class.java) {
            client.cria(request)
        }

        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)
    }

    @Test
    fun `testa tipo de conta unknown`() {
        val request = requestBuilder
            .setTipoConta(TipoConta.TIPO_CONTA_DESCONHECIDO)
            .setTipoChave(TipoChave.RANDOM)
            .setChave("")
            .build()

        Mockito.`when`(mockResponse.key).thenReturn(UUID.randomUUID().toString())
        Mockito.`when`(mockResponse.keyType).thenReturn(TipoChave.RANDOM)

        val erro = assertThrows(StatusRuntimeException::class.java) {
            client.cria(request)
        }

        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)
    }

    @Test
    fun `testa tipo de chave unknown`() {
        val request = requestBuilder
            .setTipoChave(TipoChave.TIPO_CHAVE_DESCONHECIDO)
            .setChave("")
            .build()

        Mockito.`when`(mockResponse.key).thenReturn(UUID.randomUUID().toString())
        Mockito.`when`(mockResponse.keyType).thenReturn(TipoChave.RANDOM)

        val erro = assertThrows(StatusRuntimeException::class.java) {
            client.cria(request)
        }

        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)
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