package br.com.zup.edu.chave.exceptions.handlers

import br.com.zup.edu.chave.exceptions.PixValidationException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class PixValidationExceptionHandler: PixExceptionHandler<PixValidationException> {
    override fun supports(e: RuntimeException): Boolean {
        return e is PixValidationException
    }

    override fun handle(e: PixValidationException): Status {
        return Status.INVALID_ARGUMENT.withDescription(e.localizedMessage)
    }
}