package br.com.zup.edu.chave.exceptions.handlers

import br.com.zup.edu.chave.exceptions.PixException
import br.com.zup.edu.chave.exceptions.PixNotFoundException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class PixNotFoundExceptionHandler: PixExceptionHandler<PixNotFoundException> {
    override fun supports(e: PixException): Boolean {
        return e is PixNotFoundException
    }

    override fun handle(e: PixNotFoundException): Status {
        return Status.NOT_FOUND.withDescription(e.localizedMessage)
    }
}