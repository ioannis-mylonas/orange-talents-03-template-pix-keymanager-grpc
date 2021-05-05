package br.com.zup.edu.chave.extensions

import br.com.zup.edu.DeleteKeyRequest
import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.exceptions.PixClientNotFoundException
import io.micronaut.http.client.exceptions.HttpClientResponseException

fun DeleteKeyRequest.validaCliente(client: ChaveClient) {
    val cliente = try {
        client.buscaCliente(numero)
    } catch (e: HttpClientResponseException) { null }

    cliente ?: throw PixClientNotFoundException(numero)
}

fun DeleteKeyRequest.isDono(chave: ChavePix): Boolean {
    return chave.numero == numero
}