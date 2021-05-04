package br.com.zup.edu.chave

import br.com.zup.edu.CreateKeyRequest
import br.com.zup.edu.CreateKeyResponse
import br.com.zup.edu.KeymanagerGRPCServiceGrpc
import br.com.zup.edu.Open
import br.com.zup.edu.chave.validation.PixValidator
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.ConstraintViolation

@Singleton
@Open
class KeymanagerGRPCServer(
    @Inject val validators: Collection<PixValidator>,
    @Inject val repository: ChavePixRepository
): KeymanagerGRPCServiceGrpc.KeymanagerGRPCServiceImplBase() {

    @Transactional
    override fun cria(request: CreateKeyRequest, responseObserver: StreamObserver<CreateKeyResponse>) {
        val errors = mutableSetOf<ConstraintViolation<*>>()
        validators.forEach {
            errors.addAll(it.validate(request.chave, request.tipoChave))
        }

        if (errors.isNotEmpty()) {
            var status = Status.INVALID_ARGUMENT.withDescription("Pedido inválido.")
            errors.forEach { status = status.augmentDescription(it.message) }
            responseObserver.onError(status.asRuntimeException())
            return
        }

        val chave = if (request.chave.isNullOrBlank()) UUID.randomUUID().toString() else request.chave
        val chavePix = ChavePix(request.numero, request.tipoChave, chave, request.tipoConta)
        repository.save(chavePix)

        val response = CreateKeyResponse.newBuilder()
            .setId(chavePix.id.toString())
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}