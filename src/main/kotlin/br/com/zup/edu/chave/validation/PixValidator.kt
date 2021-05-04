package br.com.zup.edu.chave.validation

import br.com.zup.edu.TipoChave
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException

interface PixValidator {
    fun shouldValidate(chave: String?, tipo: TipoChave): Boolean
    fun check(chave: String?)

    fun validate(chave: String?, tipo: TipoChave): Set<ConstraintViolation<*>> {
        if (shouldValidate(chave, tipo)) {
            try {
                check(chave)
            } catch (ex: ConstraintViolationException) {
                return ex.constraintViolations
            }
        }

        return emptySet()
    }
}