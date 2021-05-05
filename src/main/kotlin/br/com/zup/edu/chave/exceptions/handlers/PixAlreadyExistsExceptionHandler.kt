package br.com.zup.edu.chave.exceptions.handlers

import br.com.zup.edu.chave.exceptions.PixAlreadyExistsException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class PixAlreadyExistsExceptionHandler: PixExceptionHandler<PixAlreadyExistsException> {
    override fun supports(e: RuntimeException): Boolean {
        return e is PixAlreadyExistsException
    }

    override fun handle(e: PixAlreadyExistsException): Status {
        return Status.ALREADY_EXISTS.withDescription(e.localizedMessage)
    }
}