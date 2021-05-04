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

    /**
     * Chamado se a chave PIX já tiver sido cadastrada. Levanta erro de status Already Exists.
     * Processamento deve ser parado imediatamente após essa chamada.
     */
    fun alreadyExists(responseObserver: StreamObserver<CreateKeyResponse>) {
        val status = Status.ALREADY_EXISTS
            .withDescription("Chave já cadastrada.")
            .asRuntimeException()

        responseObserver.onError(status)
    }

    /**
     * Levanta erro de Invalid Argument. Deve-se parar o processamento imediatamente após essa chamada.
     */
    fun invalidArgument(responseObserver: StreamObserver<CreateKeyResponse>, errors: Set<ConstraintViolation<*>>) {
        var status = Status.INVALID_ARGUMENT.withDescription("Pedido inválido.")
        errors.forEach { status = status.augmentDescription(it.message) }
        responseObserver.onError(status.asRuntimeException())
    }

    /**
     * Chama cada um dos validadores de PIX, retornando um set de violações de constraint.
     * @return Set com as violações de restrição.
     */
    fun validate(request: CreateKeyRequest): Set<ConstraintViolation<*>> {
        val errors = mutableSetOf<ConstraintViolation<*>>()
        validators.forEach {
            errors.addAll(it.validate(request.chave, request.tipoChave))
        }
        return errors
    }

    /**
     * Valida os dados do cliente, buscando-os no ERP
     * @return False se tiver ocorrido algum erro. Nesse caso, deve-se parar o processamento imediatamente. Caso contrário, retorna true.
     */
    fun validaCliente(request: CreateKeyRequest, responseObserver: StreamObserver<CreateKeyResponse>): Boolean {
        val erro = validaCliente.valida(request.numero, request.chave, request.tipoChave, request.tipoConta)

        if (erro != null) {
            responseObserver.onError(erro)
            return false
        }
        return true
    }
}