package br.com.zup.edu.chave.exceptions

import javax.validation.ConstraintViolationException

/**
 * Alguma das restrições do cadastro de chave PIX foi violada.
 * @param e Exceção com as violações de constraint.
 */
class PixValidationException(e: ConstraintViolationException):
    PixException(e.localizedMessage)