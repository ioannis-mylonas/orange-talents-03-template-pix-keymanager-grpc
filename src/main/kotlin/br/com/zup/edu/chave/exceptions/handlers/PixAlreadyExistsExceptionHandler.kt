package br.com.zup.edu.chave.exceptions.handlers

import br.com.zup.edu.chave.exceptions.PixAlreadyExistsException
import br.com.zup.edu.chave.exceptions.PixException
import com.google.rpc.Code
import io.grpc.StatusRuntimeException
import javax.inject.Singleton

@Singleton
class PixAlreadyExistsExceptionHandler: PixExceptionHandler<PixAlreadyExistsException>() {
    override fun supports(e: PixException): Boolean {
        return e is PixAlreadyExistsException
    }

    override fun handle(e: PixAlreadyExistsException): StatusRuntimeException {
        return handle(e, Code.ALREADY_EXISTS_VALUE)
    }
}