package br.com.zup.edu.chave.cliente

import br.com.zup.edu.CreateKeyRequest
import br.com.zup.edu.DeleteKeyRequest
import br.com.zup.edu.TipoConta
import br.com.zup.edu.chave.exceptions.PixClientNotFoundException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuscaClienteDetalhes(@Inject val client: ChaveClient) {
    /**
     * Busca detalhes do cliente no ERP.
     * @param idCliente ID do cliente a ser buscado.
     * @param tipoConta Tipo de conta do cliente a ser buscada.
     * @return Detalhes do cliente, se encontrado.
     * @throws PixClientNotFoundException Se não encontrado.
     */
    fun buscaDetalhesCliente(idCliente: String, tipoConta: TipoConta): ClienteDetalhes {
        return try {
            client.buscaDetalhes(idCliente, tipoConta)
        } catch (e: HttpClientResponseException) {
            throw PixClientNotFoundException(idCliente, tipoConta)
        } ?: throw PixClientNotFoundException(idCliente, tipoConta)
    }

    /**
     * Busca cliente no ERP, por número.
     * @param idCliente ID interno do cliente.
     * @return Titular, conforme consultado.
     * @throws PixClientNotFoundException Se o cliente não for encontrado.
     */
    fun buscaTitular(idCliente: String): ClienteDetalhesTitular {
        val cliente = try {
            client.buscaCliente(idCliente)
        } catch (e: HttpClientResponseException) {
            null
        }

        cliente ?: throw PixClientNotFoundException(idCliente)
        return cliente
    }

    /**
     * Busca cliente no ERP de forma assíncrona
     * @param idCliente ID interno do cliente
     * @return Single para consulta assíncrona do cliente
     */
    fun asyncBuscaTitular(idCliente: String): Single<ClienteDetalhesTitular> {
        return Single.fromCallable { buscaTitular(idCliente) }
            .subscribeOn(Schedulers.newThread())
            .onErrorResumeNext { Single.error(it) }
    }
}