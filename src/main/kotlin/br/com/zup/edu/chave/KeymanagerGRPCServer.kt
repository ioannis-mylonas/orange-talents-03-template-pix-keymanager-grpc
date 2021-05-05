package br.com.zup.edu.chave

import br.com.zup.edu.CreateKeyRequest
import br.com.zup.edu.CreateKeyResponse
import br.com.zup.edu.KeymanagerGRPCServiceGrpc
import br.com.zup.edu.Open
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.extensions.toModel
import br.com.zup.edu.chave.extensions.validate
import br.com.zup.edu.chave.extensions.validateDadosClientes
import br.com.zup.edu.chave.extensions.validateUniqueness
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

    @Transactional
    override fun cria(request: CreateKeyRequest, responseObserver: StreamObserver<CreateKeyResponse>) {
        if (!request.validateDadosClientes(client, responseObserver)) return
        if (!request.validate(validators, responseObserver)) return
        if (!request.validateUniqueness(repository, responseObserver)) return

        val chave = request.toModel()
        repository.save(chave)

        val response = CreateKeyResponse.newBuilder()
            .setId(chave.id.toString())
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}