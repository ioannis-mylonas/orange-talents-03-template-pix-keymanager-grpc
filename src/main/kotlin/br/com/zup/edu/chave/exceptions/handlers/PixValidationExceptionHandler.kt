package br.com.zup.edu.chave.exceptions.handlers

import br.com.zup.edu.chave.exceptions.PixException
import br.com.zup.edu.chave.exceptions.PixValidationException
import com.google.rpc.Code
import com.google.rpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import javax.inject.Singleton

@Singleton
class PixValidationExceptionHandler: PixExceptionHandler<PixValidationException>() {
    override fun supports(e: PixException): Boolean {
        return e is PixValidationException
    }

    override fun handle(e: PixValidationException): StatusRuntimeException {
        val builder = Status.newBuilder()
            .setCode(Code.INVALID_ARGUMENT_VALUE)
            .setMessage(e.message)

        if (e.details != null) builder.addDetails(e.details)
        return StatusProto.toStatusRuntimeException(builder.build())
    }
}