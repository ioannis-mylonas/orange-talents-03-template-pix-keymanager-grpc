package br.com.zup.edu.chave.bcb

import br.com.zup.edu.Open
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
@Open
data class BcbDeletePixKeyRequest(
    @field:NotBlank @JsonProperty val key: String,
    @field:NotBlank @JsonProperty val participant: String = "60701190"
)