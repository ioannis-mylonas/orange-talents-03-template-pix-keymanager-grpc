package br.com.zup.edu.chave

import br.com.zup.edu.DeleteKeyRequest
import br.com.zup.edu.KeymanagerGRPCServiceGrpc
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.cliente.ClienteDetalhesTitular
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.jboss.logging.Logger
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.transaction.Transactional

@MicronautTest
internal class TestaExclusaoChavePix {
    @Inject lateinit var rpc: KeymanagerGRPCServiceGrpc.KeymanagerGRPCServiceBlockingStub
    @Inject lateinit var repository: ChavePixRepository
    @Inject lateinit var client: ChaveClient

    val DEFAULT_NUMERO = UUID.randomUUID().toString()

    @Test
    @Transactional
    fun `testa exclui chave pix existente`() {
        val titular = ClienteDetalhesTitular(DEFAULT_NUMERO, "12345678901")
        Mockito.`when`(client.buscaCliente(Mockito.anyString())).thenReturn(titular)

        var chave = ChavePix(titular.id,
            TipoChave.ALEATORIO,
            UUID.randomUUID().toString(),
            TipoConta.CONTA_CORRENTE)

        chave = repository.save(chave)
        assertTrue(repository.findAll().size == 1)

        val request = DeleteKeyRequest.newBuilder()
            .setId(chave.id)
            .setNumero(titular.id)
            .build()

        rpc.delete(request)
        assertTrue(repository.findAll().size == 0)
        // TODO Resolver ChaveNotFound no repository do servidor
    }

    @MockBean(ChaveClient::class)
    fun chaveClient(): ChaveClient {
        return Mockito.mock(ChaveClient::class.java)
    }
}