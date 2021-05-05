package br.com.zup.edu.chave.validation

import br.com.zup.edu.TipoChave
import br.com.zup.edu.chave.exceptions.PixValidationException
import javax.validation.ConstraintViolationException

/**
 * Validador com constraints para verificação da chave PIX e seu tipo
 */
interface PixValidator {
    fun shouldValidate(chave: String?, tipo: TipoChave): Boolean
    fun check(chave: String?)

    /**
     * Valida chave e tipo conforme as constraints.
     * @throws PixValidationException Se alguma das restrições foi violada.
     */
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