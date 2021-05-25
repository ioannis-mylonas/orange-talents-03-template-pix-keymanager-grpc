package br.com.zup.edu.chave.exceptions.handlers

import br.com.zup.edu.chave.exceptions.PixException
import br.com.zup.edu.chave.exceptions.PixNotFoundException
import com.google.rpc.Code
import io.grpc.StatusRuntimeException
import javax.inject.Singleton

@Singleton
class PixNotFoundExceptionHandler: PixExceptionHandler<PixNotFoundException>() {
    override fun supports(e: PixException): Boolean {
        return e is PixNotFoundException
    }

    override fun handle(e: PixNotFoundException): StatusRuntimeException {
        return handle(e, Code.NOT_FOUND_VALUE)
    }
}