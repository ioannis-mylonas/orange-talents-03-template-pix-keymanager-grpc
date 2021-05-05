package br.com.zup.edu.chave.cliente

import br.com.zup.edu.Open
import br.com.zup.edu.TipoConta
import com.fasterxml.jackson.annotation.JsonProperty

@Open
data class ClienteDetalhes(
    @JsonProperty val tipo: TipoConta,
    @JsonProperty val instituicao: ClienteDetalhesInstituicao,
    @JsonProperty val agencia: String,
    @JsonProperty val numero: String,
    @JsonProperty val titular: ClienteDetalhesTitular
    )