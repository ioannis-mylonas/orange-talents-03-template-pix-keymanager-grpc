package br.com.zup.edu.chave.bcb

import br.com.zup.edu.TipoConta
import br.com.zup.edu.chave.cliente.ClienteDetalhes

enum class BcbAccountType {
    CACC, SVGS;

    companion object {
        fun fromClienteDetalhes(detalhes: ClienteDetalhes): BcbAccountType {
            return when (detalhes.tipo) {
                TipoConta.CONTA_CORRENTE -> CACC
                TipoConta.CONTA_POUPANCA -> SVGS
                else -> throw IllegalArgumentException("Conta do cliente não é corrente nem poupança.")
            }
        }
    }
}