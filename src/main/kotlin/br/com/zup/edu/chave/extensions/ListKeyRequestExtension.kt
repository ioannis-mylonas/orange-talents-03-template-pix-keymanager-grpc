package br.com.zup.edu.chave.extensions

import br.com.zup.edu.DeleteKeyRequest
import br.com.zup.edu.ListKeyRequest
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.cliente.ClienteDetalhesTitular
import br.com.zup.edu.chave.exceptions.PixClientNotFoundException
import io.micronaut.http.client.exceptions.HttpClientResponseException

/**
 * Busca cliente no ERP, por número.
 * @param client Um client para consulta no ERP.
 * @return Titular, conforme consultado.
 * @throws PixClientNotFoundException Se o cliente não for encontrado.
 */
fun ListKeyRequest.buscaTitular(client: ChaveClient): ClienteDetalhesTitular {
    val cliente = try {
        client.buscaCliente(idCliente)
    } catch (e: HttpClientResponseException) { null }

    cliente ?: throw PixClientNotFoundException(idCliente)
    return cliente
}