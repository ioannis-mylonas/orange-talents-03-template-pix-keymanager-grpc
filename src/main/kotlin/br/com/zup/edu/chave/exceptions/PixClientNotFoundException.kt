package br.com.zup.edu.chave.exceptions

import br.com.zup.edu.TipoConta

/**
 * Conta do cliente não foi encontrada no ERP. Pode ser por número não cadastrado
 * ou porque o cliente não tem uma conta desse tipo.
 * @param numero Número da conta do cliente.
 */
class PixClientNotFoundException(numero: String):
    PixNotFoundException("Cliente de número $numero não encontrado") {

    /**
     * Conta do cliente não foi encontrada no ERP. Pode ser por número não cadastrado
     * ou porque o cliente não tem uma conta desse tipo.
     * @param numero Número da conta do cliente.
     * @param tipo Tipo de conta que foi buscada.
     */
    constructor(numero: String, tipo: TipoConta) : this("Cliente de número $numero e tipo de conta $tipo não encontrado")
    }