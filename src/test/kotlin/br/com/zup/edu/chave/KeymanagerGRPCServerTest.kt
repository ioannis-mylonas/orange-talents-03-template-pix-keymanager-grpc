package br.com.zup.edu.chave

import br.com.zup.edu.*
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.cliente.ClienteDetalhes
import br.com.zup.edu.chave.cliente.ClienteDetalhesTitular
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EmptySource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest
internal class KeymanagerGRPCServerTest {
//    @Inject lateinit var client: KeymanagerGRPCServiceGrpc.KeymanagerGRPCServiceBlockingStub
//
//    private val DEFAULT_NUMERO = UUID.randomUUID().toString()
//    private val DEFAULT_TIPO_CONTA = TipoConta.CONTA_CORRENTE
//    private val DEFAULT_TIPO_CHAVE = TipoChave.CPF
//    private val DEFAULT_CHAVE = "12345678901"
//
//    private val requestBuilder = CreateKeyRequest.newBuilder()
//        .setNumero(DEFAULT_NUMERO)
//        .setTipoConta(DEFAULT_TIPO_CONTA)
//        .setTipoChave(DEFAULT_TIPO_CHAVE)
//        .setChave(DEFAULT_CHAVE)
//
//    @MockBean(ChaveClient::class)
//    fun chaveClient(): ChaveClient {
//        val result = Mockito.mock(ChaveClient::class.java)
//
//        // Não consegui fazer funcionar `when`(chaveClient().buscaDetalhes(Mockito.anyString(), MockitoHelper.anyObject()))
//        Mockito.`when`(result.buscaDetalhes(Mockito.anyString(), MockitoHelper.anyObject()))
//            .thenReturn(mockDetalhes)
//
//        return result
//    }
//
//    private val mockDetalhes = Mockito.mock(ClienteDetalhes::class.java, Mockito.RETURNS_DEEP_STUBS)
//
//    @ParameterizedTest
//    @EmptySource
//    @ValueSource(strings = ["1", "123", "123.456.789-30", "123456789-30"])
//    fun `testa requisicao CPF pattern invalida`(cpf: String) {
//        Mockito.`when`(mockDetalhes.titular.cpf).thenReturn(cpf)
//
//        val request = requestBuilder
//            .setTipoChave(TipoChave.CPF)
//            .setChave(cpf)
//            .build()
//
//        val erro = assertThrows(StatusRuntimeException::class.java) {
//            client.cria(request)
//        }
//
//        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)
//    }
//
//    @ParameterizedTest
//    @ValueSource(strings = ["12345678901", "01234567890", "10320064042", "42423029080"])
//    fun `testa requisicao CPF valido`(cpf: String) {
//        Mockito.`when`(mockDetalhes.titular.cpf).thenReturn(cpf)
//
//        val request = requestBuilder
//            .setTipoChave(TipoChave.CPF)
//            .setChave(cpf)
//            .build()
//
//        client.cria(request)
//    }
//
//    @ParameterizedTest
//    @EmptySource
//    @ValueSource(strings = ["emailinvalido", "naomail@", "@gmail.com.br", "eeeemail@ gmail.com"])
//    fun `testa requisicao email invalido`(email: String) {
//        val request = requestBuilder
//            .setTipoChave(TipoChave.EMAIL)
//            .setChave(email)
//            .build()
//
//        val erro = assertThrows(StatusRuntimeException::class.java) {
//            client.cria(request)
//        }
//
//        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)
//    }
//
//    @ParameterizedTest
//    @ValueSource(strings = ["joao@email.com", "maria@gmail.com", "eduardo@zup.com.br", "joe@mail"])
//    fun `testa requisicao email valido`(email: String) {
//        val request = requestBuilder
//            .setTipoChave(TipoChave.EMAIL)
//            .setChave(email)
//            .build()
//
//        client.cria(request)
//    }
//
//    @ParameterizedTest
//    @EmptySource
//    @ValueSource(strings = ["1", "123", "+12", "+12345678912345678"])
//    fun `testa requisicao celular invalido`(celular: String) {
//        val request = requestBuilder
//            .setTipoChave(TipoChave.CELULAR)
//            .setChave(celular)
//            .build()
//
//        val erro = assertThrows(StatusRuntimeException::class.java) {
//            client.cria(request)
//        }
//
//        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)
//    }
//
//    @ParameterizedTest
//    @ValueSource(strings = ["+123", "+12345678912345", "+5585988714077"])
//    fun `testa requisicao celular valido`(celular: String) {
//        val request = requestBuilder
//            .setTipoChave(TipoChave.CELULAR)
//            .setChave(celular)
//            .build()
//
//        client.cria(request)
//    }
//
//    @ParameterizedTest
//    @ValueSource(strings = ["a", "+5585988714077", "joao@email.com", "42423029080"])
//    fun `testa requisicao aleatorio invalido`(aleatorio: String) {
//        val request = requestBuilder
//            .setTipoChave(TipoChave.ALEATORIO)
//            .setChave(aleatorio)
//            .build()
//
//        val erro = assertThrows(StatusRuntimeException::class.java) {
//            client.cria(request)
//        }
//
//        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)
//    }
//
//    @ParameterizedTest
//    @EmptySource
//    fun `testa requisicao aleatorio valido`(aleatorio: String) {
//        val request = requestBuilder
//            .setTipoChave(TipoChave.ALEATORIO)
//            .setChave(aleatorio)
//            .build()
//
//        client.cria(request)
//    }
//
//    @Factory
//    class Clients {
//        @Singleton
//        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerGRPCServiceGrpc.KeymanagerGRPCServiceBlockingStub {
//            return KeymanagerGRPCServiceGrpc.newBlockingStub(channel)
//        }
//    }
}