package br.com.zup.edu.chave.extensions

import br.com.zup.edu.DeleteKeyRequest
import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.exceptions.PixClientNotFoundException
import io.micronaut.http.client.exceptions.HttpClientResponseException

/**
 * Busca cliente no ERP, por número.
 * @param client Um client para consulta no ERP.
 * @throws PixClientNotFoundException Se o cliente não for encontrado.
 */
fun DeleteKeyRequest.validaCliente(client: ChaveClient) {
    val cliente = try {
        client.buscaCliente(numero)
    } catch (e: HttpClientResponseException) { null }

    cliente ?: throw PixClientNotFoundException(numero)
}

/**
 * Verifica se o cliente é dono da chave.
 * @param chave Chave PIX a ser verificada.
 * @return True se o cliente for dono, false caso contrário.
 */
fun DeleteKeyRequest.isDono(chave: ChavePix): Boolean {
    return chave.numero == numero
}