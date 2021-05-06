package br.com.zup.edu.chave.bcb

import br.com.zup.edu.TipoChave
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class BcbKeyDetails(
    @JsonProperty val keyType: TipoChave,
    @JsonProperty val key: String,
    @JsonProperty val bankAccount: BcbBankAccount,
    @JsonProperty val owner: BcbOwner,
    @JsonProperty val createdAt: LocalDateTime
)