package br.com.zup.edu.chave

import br.com.zup.edu.*
import br.com.zup.edu.chave.bcb.BcbClient
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.exceptions.*
import br.com.zup.edu.chave.exceptions.handlers.ExceptionInterceptor
import br.com.zup.edu.chave.extensions.*
import br.com.zup.edu.chave.validation.PixValidator
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Open
class KeymanagerGRPCServer(
    @Inject val validators: Collection<PixValidator>,
    @Inject val repository: ChavePixRepository,
    @Inject val client: ChaveClient,
    @Inject val bcbClient: BcbClient
): KeymanagerGRPCServiceGrpc.KeymanagerGRPCServiceImplBase() {
    @ExceptionInterceptor
    override fun cria(request: CreateKeyRequest, responseObserver: StreamObserver<CreateKeyResponse>) {
        val detalhes = request.buscaDetalhesCliente(client)

        request.validaDadosClientes(detalhes)
        request.valida(validators)
        request.validaUniqueness(repository)

        val chave = repository.saveBcb(request, detalhes, bcbClient)

        val response = CreateKeyResponse.newBuilder()
            .setId(chave.id)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    @ExceptionInterceptor
    override fun delete(request: DeleteKeyRequest, responseObserver: StreamObserver<DeleteKeyResponse>) {
        val dados = request.buscaTitular(client)

        val chave = repository.findById(request.id).orElseThrow { PixChaveNotFound() }
        if (!request.isDono(chave, dados)) throw PixClientKeyPermissionException()

        repository.deleteBcb(chave, bcbClient)

        // Se eventualmente o retorno mudar, temos um builder aqui
        val response = DeleteKeyResponse.newBuilder().build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}