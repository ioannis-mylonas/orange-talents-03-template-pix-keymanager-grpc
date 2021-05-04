package br.com.zup.edu.chave.cliente

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import io.grpc.Status
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Singleton

@Singleton
class ValidaCliente(private val client: ChaveClient) {
    /**
     * Valida dados do cliente conforme ERP.
     * @return Status como RuntimeException com o erro ocorrido (se ocorrido). Nesse caso, deve-se passar o erro ao observer e parar o processamento.
     */
    fun valida(id: String, chave: String, tipoChave: TipoChave, tipoConta: TipoConta): RuntimeException? {
        // Busca dados do cliente no ERP com ID e tipo de conta. Caso null, cliente não foi encontrado
        val detalhes = client.buscaDetalhes(id, tipoConta) ?: return Status.NOT_FOUND
            .withDescription("Conta não encontrada no sistema.")
            .asRuntimeException()

        // Caso o CPF informado não corresponda ao CPF encontrado no ERP, permissão negada
        if (tipoChave == TipoChave.CPF && chave != detalhes.titular.cpf) {
            return Status
                .PERMISSION_DENIED
                .withDescription("CPF não corresponde ao usuário cadastrado")
                .asRuntimeException()
        }

        // Nenhum problema encontrado com os dados do cliente
        return null
    }
}