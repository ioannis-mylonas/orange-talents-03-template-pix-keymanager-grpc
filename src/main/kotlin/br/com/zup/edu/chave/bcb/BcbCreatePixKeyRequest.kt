package br.com.zup.edu.chave.bcb

import br.com.zup.edu.CreateKeyRequest
import br.com.zup.edu.Open
import br.com.zup.edu.TipoChave
import br.com.zup.edu.chave.cliente.ClienteDetalhes
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@Open
data class BcbCreatePixKeyRequest(
    @field:NotNull @JsonProperty val keyType: TipoChave,
    @field:Size(max = 77) @JsonProperty val key: String,
    @field:NotNull @field:Valid @JsonProperty val bankAccount: BcbBankAccount,
    @field:NotNull @field:Valid @JsonProperty val owner: BcbOwner
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