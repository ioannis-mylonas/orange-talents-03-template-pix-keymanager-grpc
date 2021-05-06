package br.com.zup.edu.chave.extensions

import br.com.zup.edu.DeleteKeyRequest
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.cliente.ClienteDetalhesTitular
import br.com.zup.edu.chave.exceptions.PixClientNotFoundException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*
import kotlin.random.Random

@MicronautTest(transactional = false, rollback = false)
internal class DeleteKeyRequestExtensionKtTest {
    private val client: ChaveClient = Mockito.mock(ChaveClient::class.java)

    private val DEFAULT_NUMERO = UUID.randomUUID().toString()
    private val DEFAULT_ID = 1L

    @Test
    fun `testa nao encontra cliente no erp`() {
        Mockito.`when`(client.buscaCliente(Mockito.anyString())).thenReturn(null)
        val request = DeleteKeyRequest.newBuilder()
            .setId(DEFAULT_ID)
            .setNumero(DEFAULT_NUMERO)
            .build()

        assertThrows(PixClientNotFoundException::class.java) {
            request.validaCliente(client)
        }
    }

    @Test
    fun `testa nao encontra cliente no erp http client response exception`() {
        Mockito.`when`(client.buscaCliente(Mockito.anyString()))
            .thenThrow(HttpClientResponseException::class.java)

        val request = DeleteKeyRequest.newBuilder()
            .setId(DEFAULT_ID)
            .setNumero(DEFAULT_NUMERO)
            .build()

        assertThrows(PixClientNotFoundException::class.java) {
            request.validaCliente(client)
        }
    }

    @Test
    fun `testa is dono true`() {
        val cpf = UUID.randomUUID().toString()
        val key = UUID.randomUUID().toString()

        val titular = ClienteDetalhesTitular("ID", "NOME", cpf)
        val chave = ChavePix(TipoChave.RANDOM, key, TipoConta.CONTA_CORRENTE, cpf)
        val request = DeleteKeyRequest
            .newBuilder()
            .setNumero(cpf)
            .setId(chave.id)
            .build()

        assertTrue(request.isDono(chave, titular))
    }

    @Test
    fun `testa is dono false cpf errado`() {
        val cpf = UUID.randomUUID().toString()
        val key = UUID.randomUUID().toString()

        val titular = ClienteDetalhesTitular("ID", "NOME", UUID.randomUUID().toString())
        val chave = ChavePix(TipoChave.RANDOM, key, TipoConta.CONTA_CORRENTE, cpf)
        val request = DeleteKeyRequest.newBuilder()
            .setNumero(UUID.randomUUID().toString())
            .setId(chave.id)
            .build()

        assertTrue(!request.isDono(chave, titular))
    }

    @Test
    fun `testa is dono false id errado`() {
        val cpf = UUID.randomUUID().toString()
        val key = UUID.randomUUID().toString()

        val titular = ClienteDetalhesTitular("ID", "NOME", cpf)
        val chave = ChavePix(TipoChave.RANDOM, key, TipoConta.CONTA_CORRENTE, cpf)
        val request = DeleteKeyRequest.newBuilder()
            .setNumero(cpf)
            .setId(Random.nextLong())
            .build()

        assertTrue(!request.isDono(chave, titular))
    }
}