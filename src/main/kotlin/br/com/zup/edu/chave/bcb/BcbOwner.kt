package br.com.zup.edu.chave.bcb

import br.com.zup.edu.Open
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Open
data class BcbOwner(
    @field:NotNull @JsonProperty val type: BcbOwnerType,
    @field:NotBlank @JsonProperty val name: String,
    @field:NotBlank @JsonProperty val taxIdNumber: String
)