package br.com.zup.edu.chave.bcb

import br.com.zup.edu.Open
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Open
data class BcbOwner(
    @NotNull @JsonProperty val type: BcbOwnerType,
    @NotBlank @JsonProperty val name: String,
    @NotBlank @JsonProperty val taxIdNumber: String
)