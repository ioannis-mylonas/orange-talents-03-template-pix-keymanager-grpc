package br.com.zup.edu.chave.exceptions.handlers

import br.com.zup.edu.chave.exceptions.PixClientWrongCpfException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class PixClientWrongCpfExceptionHandler: PixExceptionHandler<PixClientWrongCpfException> {
    override fun supports(e: RuntimeException): Boolean {
        return e is PixClientWrongCpfException
    }

    override fun handle(e: PixClientWrongCpfException): Status {
        return Status.PERMISSION_DENIED.withDescription(e.localizedMessage)
    }
}