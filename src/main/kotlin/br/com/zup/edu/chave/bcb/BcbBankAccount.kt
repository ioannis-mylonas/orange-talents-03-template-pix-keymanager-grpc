package br.com.zup.edu.chave.bcb

import br.com.zup.edu.Open
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@Open
data class BcbBankAccount(
    @field:NotBlank @field:Size(min = 4, max = 4) @JsonProperty val branch: String,
    @field:NotBlank @field:Size(min = 6, max = 6) @JsonProperty val accountNumber: String,
    @field:NotNull @JsonProperty val accountType: BcbAccountType,
    @field:NotBlank @JsonProperty val participant: String = "60701190"
)