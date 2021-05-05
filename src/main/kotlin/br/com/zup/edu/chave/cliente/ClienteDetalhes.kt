package br.com.zup.edu.chave.cliente

import br.com.zup.edu.Open
import com.fasterxml.jackson.annotation.JsonProperty

@Open
data class ClienteDetalhes(
    @JsonProperty val titular: ClienteDetalhesTitular
    )