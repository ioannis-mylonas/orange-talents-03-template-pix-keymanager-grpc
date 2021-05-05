package br.com.zup.edu.chave.exceptions

import javax.validation.ConstraintViolationException

class PixValidationException(e: ConstraintViolationException):
    PixException(e.localizedMessage)