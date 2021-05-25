package br.com.zup.edu.chave.exceptions.handlers

import br.com.zup.edu.chave.exceptions.PixException
import br.com.zup.edu.chave.exceptions.PixPermissionDeniedException
import com.google.rpc.Code
import io.grpc.StatusRuntimeException
import javax.inject.Singleton

@Singleton
class PixPermissionDeniedExceptionHandler: PixExceptionHandler<PixPermissionDeniedException>() {
    override fun supports(e: PixException): Boolean {
        return e is PixPermissionDeniedException
    }

    override fun handle(e: PixPermissionDeniedException): StatusRuntimeException {
        return handle(e, Code.PERMISSION_DENIED_VALUE)
    }
}