package br.com.zup.edu.chave.extensions

import br.com.zup.edu.GetKeyResponse
import br.com.zup.edu.chave.cliente.ClienteDetalhes

fun GetKeyResponse.Titular.fromDetalhes(detalhes: ClienteDetalhes): GetKeyResponse.Titular {
    return GetKeyResponse.Titular.newBuilder()
        .setCpf(detalhes.titular.cpf)
        .setNome(detalhes.titular.nome)
        .build()
}

fun GetKeyResponse.Instituicao.fromDetalhes(detalhes: ClienteDetalhes): GetKeyResponse.Instituicao {
    return GetKeyResponse.Instituicao.newBuilder()
        .setAgencia(detalhes.agencia)
        .setConta(detalhes.numero)
        .setNome(detalhes.instituicao.nome)
        .setTipoConta(detalhes.tipo)
        .build()
}