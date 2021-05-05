package br.com.zup.edu.chave.exceptions.handlers

import br.com.zup.edu.chave.exceptions.PixException
import br.com.zup.edu.chave.exceptions.PixPermissionDeniedException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class PixPermissionDeniedExceptionHandler: PixExceptionHandler<PixPermissionDeniedException> {
    override fun supports(e: PixException): Boolean {
        return e is PixPermissionDeniedException
    }

    override fun handle(e: PixPermissionDeniedException): Status {
        return Status.PERMISSION_DENIED.withDescription(e.localizedMessage)
    }
}