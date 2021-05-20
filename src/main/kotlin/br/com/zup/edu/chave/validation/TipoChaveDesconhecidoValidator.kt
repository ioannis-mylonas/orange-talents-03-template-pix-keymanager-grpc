package br.com.zup.edu.chave.validation

import br.com.zup.edu.TipoChave
import br.com.zup.edu.chave.exceptions.PixTipoChaveUnknownException
import javax.inject.Singleton

@Singleton
class TipoChaveDesconhecidoValidator: PixValidator {
    override fun shouldValidate(chave: String?, tipo: TipoChave): Boolean {
        return (tipo == TipoChave.TIPO_CHAVE_DESCONHECIDO || tipo == TipoChave.UNRECOGNIZED)
    }

    override fun check(chave: String?) {
        throw PixTipoChaveUnknownException()
    }
}