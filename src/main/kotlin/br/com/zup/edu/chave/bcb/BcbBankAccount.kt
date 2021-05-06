package br.com.zup.edu.chave.bcb

import br.com.zup.edu.Open
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Open
data class BcbBankAccount(
    @NotBlank @Size(min = 4, max = 4) @JsonProperty val branch: String,
    @NotBlank @Size(min = 6, max = 6) @JsonProperty val accountNumber: String,
    @NotNull @JsonProperty val accountType: BcbAccountType,
    @NotBlank @JsonProperty val participant: String = "60701190"
)