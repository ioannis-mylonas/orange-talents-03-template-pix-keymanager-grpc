package br.com.zup.edu.chave.exceptions

import br.com.zup.edu.TipoConta

class PixClientNotFoundException(numero: String, conta: TipoConta):
    PixException("Cliente de número $numero e tipo de conta $conta não encontrado.")