package br.com.zup.edu.chave.cliente

import com.fasterxml.jackson.annotation.JsonProperty

data class ClienteDetalhesTitular(
    @JsonProperty val id: String,
    @JsonProperty val nome: String,
    @JsonProperty val cpf: String
)