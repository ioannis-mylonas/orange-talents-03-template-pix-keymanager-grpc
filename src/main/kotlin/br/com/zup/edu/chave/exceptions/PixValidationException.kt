package br.com.zup.edu.chave.exceptions

import javax.validation.ConstraintViolationException

/**
 * Alguma das restrições do cadastro de chave PIX foi violada.
 * @param e Exceção com as violações de constraint.
 */
class PixValidationException(message: String?): PixException(message) {
        constructor(e: ConstraintViolationException) : this(e.localizedMessage)
    }