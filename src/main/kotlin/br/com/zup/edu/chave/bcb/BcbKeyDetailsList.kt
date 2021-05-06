package br.com.zup.edu.chave.bcb

import com.fasterxml.jackson.annotation.JsonProperty

data class BcbKeyDetailsList(
    @JsonProperty val pixKeys: List<BcbKeyDetails>
)