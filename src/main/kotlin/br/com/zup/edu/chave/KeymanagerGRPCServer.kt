package br.com.zup.edu.chave

import br.com.zup.edu.CreateKeyRequest
import br.com.zup.edu.CreateKeyResponse
import br.com.zup.edu.KeymanagerGRPCServiceGrpc
import br.com.zup.edu.Open
import br.com.zup.edu.chave.cliente.ValidaCliente
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
    @Inject val repository: ChavePixRepository,
    @Inject val validaCliente: ValidaCliente
): KeymanagerGRPCServiceGrpc.KeymanagerGRPCServiceImplBase() {

    @Transactional
    override fun cria(request: CreateKeyRequest, responseObserver: StreamObserver<CreateKeyResponse>) {
        if (repository.existsByChave(request.chave)) return alreadyExists(responseObserver)
        if (!validaCliente(request, responseObserver)) return

        val errors = validate(request)
        if (errors.isNotEmpty()) return invalidArgument(responseObserver, errors)

        val chave = if (request.chave.isNullOrBlank()) UUID.randomUUID().toString() else request.chave
        val chavePix = ChavePix(request.numero, request.tipoChave, chave, request.tipoConta)
        repository.save(chavePix)

        val response = CreateKeyResponse.newBuilder()
            .setId(chavePix.id.toString())
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    fun alreadyExists(responseObserver: StreamObserver<CreateKeyResponse>) {
        val status = Status.ALREADY_EXISTS
            .withDescription("Chave já cadastrada.")
            .asRuntimeException()

        responseObserver.onError(status)
    }

    fun invalidArgument(responseObserver: StreamObserver<CreateKeyResponse>, errors: Set<ConstraintViolation<*>>) {
        var status = Status.INVALID_ARGUMENT.withDescription("Pedido inválido.")
        errors.forEach { status = status.augmentDescription(it.message) }
        responseObserver.onError(status.asRuntimeException())
    }

    fun validate(request: CreateKeyRequest): Set<ConstraintViolation<*>> {
        val errors = mutableSetOf<ConstraintViolation<*>>()
        validators.forEach {
            errors.addAll(it.validate(request.chave, request.tipoChave))
        }
        return errors
    }

    fun validaCliente(request: CreateKeyRequest, responseObserver: StreamObserver<CreateKeyResponse>): Boolean {
        try {
            validaCliente.valida(request.numero, request.chave, request.tipoChave, request.tipoConta)
            return true
        } catch (e: RuntimeException) {
            responseObserver.onError(e)
            return false
        }
    }
}