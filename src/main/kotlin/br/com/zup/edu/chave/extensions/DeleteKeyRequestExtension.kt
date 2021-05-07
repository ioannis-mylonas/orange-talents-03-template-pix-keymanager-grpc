package br.com.zup.edu.chave.extensions

import br.com.zup.edu.DeleteKeyRequest
import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.cliente.ClienteDetalhesTitular
import br.com.zup.edu.chave.exceptions.PixClientNotFoundException
import io.micronaut.http.client.exceptions.HttpClientResponseException

/**
 * Verifica se o cliente é dono da chave.
 * @param chave Chave PIX a ser verificada.
 * @return True se o cliente for dono, false caso contrário.
 */
fun DeleteKeyRequest.isDono(chave: ChavePix, titular: ClienteDetalhesTitular): Boolean {
    return (titular.cpf == chave.cpf &&
            chave.id == idPix)
}