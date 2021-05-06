package br.com.zup.edu.chave.cliente

import com.fasterxml.jackson.annotation.JsonProperty

data class ClienteDetalhesInstituicao(
    @JsonProperty val nome: String,
    @JsonProperty val ispb: String
)