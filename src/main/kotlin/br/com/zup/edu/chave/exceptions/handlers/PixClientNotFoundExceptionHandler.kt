package br.com.zup.edu.chave.exceptions.handlers

import br.com.zup.edu.chave.exceptions.PixClientNotFoundException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class PixClientNotFoundExceptionHandler: PixExceptionHandler<PixClientNotFoundException> {
    override fun supports(e: RuntimeException): Boolean {
        return e is PixClientNotFoundException
    }

    override fun handle(e: PixClientNotFoundException): Status {
        return Status.NOT_FOUND.withDescription(e.localizedMessage)
    }
}