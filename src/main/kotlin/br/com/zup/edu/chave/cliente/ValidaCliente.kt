package br.com.zup.edu.chave.cliente

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import io.grpc.Status
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Singleton

@Singleton
class ValidaCliente(private val client: ChaveClient) {
    fun valida(id: String, chave: String, tipoChave: TipoChave, tipoConta: TipoConta): RuntimeException? {
        val detalhes = client.buscaDetalhes(id, tipoConta) ?: return Status.NOT_FOUND
            .withDescription("Conta não encontrada no sistema.")
            .asRuntimeException()

        if (tipoChave == TipoChave.CPF && chave != detalhes.titular.cpf) {
            return Status
                .PERMISSION_DENIED
                .withDescription("CPF não corresponde ao usuário cadastrado")
                .asRuntimeException()
        }

        return null
    }
}