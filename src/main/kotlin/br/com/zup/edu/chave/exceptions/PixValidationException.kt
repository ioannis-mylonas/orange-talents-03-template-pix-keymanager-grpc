package br.com.zup.edu.chave.exceptions

import br.com.zup.edu.chave.extensions.toPackedBadRequest
import javax.validation.ConstraintViolationException

/**
 * Alguma das restrições do cadastro de chave PIX foi violada.
 * @param e Exceção com as violações de constraint.
 */
open class PixValidationException(message: String?, val details: com.google.protobuf.Any? = null): PixException(message) {
        constructor(e: ConstraintViolationException) : this(
            e.localizedMessage,
            e.constraintViolations.toPackedBadRequest()
        )
    }