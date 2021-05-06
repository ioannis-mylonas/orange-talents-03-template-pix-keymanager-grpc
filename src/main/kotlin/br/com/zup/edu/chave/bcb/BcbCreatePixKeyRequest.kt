package br.com.zup.edu.chave.bcb

import br.com.zup.edu.CreateKeyRequest
import br.com.zup.edu.Open
import br.com.zup.edu.TipoChave
import br.com.zup.edu.chave.cliente.ClienteDetalhes
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected

@Introspected
@Open
data class BcbCreatePixKeyRequest(
    @JsonProperty val keyType: TipoChave,
    @JsonProperty val key: String,
    @JsonProperty val bankAccount: BcbBankAccount,
    @JsonProperty val owner: BcbOwner
) {
    companion object {
        fun fromDetalhesCliente(detalhes: ClienteDetalhes, request: CreateKeyRequest): BcbCreatePixKeyRequest {
            val account = BcbBankAccount(
                detalhes.agencia,
                detalhes.numero,
                BcbAccountType.fromClienteDetalhes(detalhes))

            val owner = BcbOwner(BcbOwnerType.NATURAL_PERSON,
                detalhes.titular.nome,
                detalhes.titular.cpf)

            return BcbCreatePixKeyRequest(request.tipoChave, request.chave, account, owner)
        }
    }
}