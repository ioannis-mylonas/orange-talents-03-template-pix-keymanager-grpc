package br.com.zup.edu.chave.cliente

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import io.grpc.Status
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Singleton

@Singleton
class ValidaCliente(private val client: ChaveClient) {
    fun valida(id: String, chave: String, tipoChave: TipoChave, tipoConta: TipoConta) {
        try {
            val detalhes = client.buscaDetalhes(id, tipoConta)

            if (tipoChave == TipoChave.CPF && chave != detalhes.titular.cpf) {
                throw Status
                    .PERMISSION_DENIED
                    .withDescription("CPF não corresponde ao usuário cadastrado")
                    .asRuntimeException()
            }
        } catch (e: HttpClientResponseException) {
            throw Status.NOT_FOUND
                .withCause(e)
                .withDescription("Conta não encontrada no sistema.")
                .augmentDescription(e.localizedMessage)
                .asRuntimeException()
        }
    }
}