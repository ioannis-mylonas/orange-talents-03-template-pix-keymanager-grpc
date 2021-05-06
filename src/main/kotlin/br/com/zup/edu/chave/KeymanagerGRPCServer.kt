package br.com.zup.edu.chave

import br.com.zup.edu.*
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.exceptions.PixChaveNotFound
import br.com.zup.edu.chave.exceptions.PixClientKeyPermissionException
import br.com.zup.edu.chave.exceptions.handlers.ExceptionInterceptor
import br.com.zup.edu.chave.extensions.*
import br.com.zup.edu.chave.validation.PixValidator
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
@Open
class KeymanagerGRPCServer(
    @Inject val validators: Collection<PixValidator>,
    @Inject val repository: ChavePixRepository,
    @Inject val client: ChaveClient
): KeymanagerGRPCServiceGrpc.KeymanagerGRPCServiceImplBase() {

    @ExceptionInterceptor
    override fun cria(request: CreateKeyRequest, responseObserver: StreamObserver<CreateKeyResponse>) {
        request.validateDadosClientes(client)
        request.validate(validators)
        request.validateUniqueness(repository)

        val chave = request.toModel()
        repository.save(chave)

        val response = CreateKeyResponse.newBuilder()
            .setId(chave.id)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    @ExceptionInterceptor
    override fun delete(request: DeleteKeyRequest, responseObserver: StreamObserver<DeleteKeyResponse>) {
        request.validaCliente(client)

        val chave = repository.findById(request.id).orElseThrow { PixChaveNotFound() }
        if (!request.isDono(chave)) throw PixClientKeyPermissionException()

        repository.delete(chave)

        // Se eventualmente o retorno mudar, temos um builder aqui
        val response = DeleteKeyResponse.newBuilder().build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}