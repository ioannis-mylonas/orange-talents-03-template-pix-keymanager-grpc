package br.com.zup.edu.chave.extensions

import br.com.zup.edu.*
import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.cliente.ClienteDetalhes
import br.com.zup.edu.chave.cliente.ClienteDetalhesTitular
import br.com.zup.edu.chave.exceptions.PixClientKeyPermissionException
import br.com.zup.edu.chave.exceptions.PixClientNotFoundException
import io.micronaut.http.client.exceptions.HttpClientResponseException

/**
 * Valida se o cliente é dono da chave.
 * @param chave A chave PIX alvo
 * @param titular O cliente que está tentando acessar a chave
 * @throws PixClientKeyPermissionException Se o cliente não tiver permissão
 */
fun GetKeyRequest.validaDono(chave: ChavePix, titular: ClienteDetalhesTitular) {
    if (!isDono(chave, titular)) throw PixClientKeyPermissionException()
}

/**
 * Verifica se o cliente é dono da chave.
 * @param chave Chave PIX a ser verificada.
 * @param titular Titular a ser verificado.
 * @return True se o cliente for dono, false caso contrário.
 */
fun GetKeyRequest.isDono(chave: ChavePix, titular: ClienteDetalhesTitular): Boolean {
    return (titular.id == chave.idCliente &&
            chave.id == idPix)
}