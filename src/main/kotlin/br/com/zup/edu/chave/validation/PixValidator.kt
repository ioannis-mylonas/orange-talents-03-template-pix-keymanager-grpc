package br.com.zup.edu.chave.validation

import br.com.zup.edu.TipoChave
import br.com.zup.edu.chave.exceptions.PixValidationException
import javax.validation.ConstraintViolationException

interface PixValidator {
    fun shouldValidate(chave: String?, tipo: TipoChave): Boolean
    fun check(chave: String?)

    fun validate(chave: String?, tipo: TipoChave) {
        if (shouldValidate(chave, tipo)) {
            try {
                check(chave)
            } catch (e: ConstraintViolationException) {
                throw PixValidationException(e)
            }
        }
    }
}