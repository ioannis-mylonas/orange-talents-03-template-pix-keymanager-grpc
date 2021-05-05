package br.com.zup.edu.chave.exceptions

import br.com.zup.edu.TipoConta

/**
 * Conta do cliente não foi encontrada no ERP. Pode ser por número não cadastrado
 * ou porque o cliente não tem uma conta desse tipo.
 */
class PixClientNotFoundException(numero: String, conta: TipoConta):
    PixException("Cliente de número $numero e tipo de conta $conta não encontrado.")