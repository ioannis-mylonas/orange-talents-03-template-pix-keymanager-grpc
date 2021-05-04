package br.com.zup.edu.chave.validation

import br.com.zup.edu.Open
import br.com.zup.edu.TipoChave
import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.constraints.IsEmpty
import br.com.zup.edu.constraints.Unique
import io.micronaut.validation.validator.Validator
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException

@Singleton
@Open
class AleatorioPixValidator: PixValidator {
    override fun shouldValidate(chave: String?, tipo: TipoChave): Boolean {
        return tipo == TipoChave.ALEATORIO
    }

    override fun check(@IsEmpty chave: String?) {}
}