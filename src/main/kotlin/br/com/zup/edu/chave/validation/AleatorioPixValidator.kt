package br.com.zup.edu.chave.validation

import br.com.zup.edu.Open
import br.com.zup.edu.TipoChave
import br.com.zup.edu.constraints.IsEmpty
import javax.inject.Singleton

@Singleton
@Open
class AleatorioPixValidator: PixValidator {
    override fun shouldValidate(chave: String?, tipo: TipoChave): Boolean {
        return tipo == TipoChave.ALEATORIO
    }

    override fun check(@IsEmpty chave: String?) {}
}