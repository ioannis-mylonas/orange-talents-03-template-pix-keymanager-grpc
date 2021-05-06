package br.com.zup.edu.chave

import br.com.zup.edu.*
import br.com.zup.edu.chave.bcb.BcbClient
import br.com.zup.edu.chave.bcb.BcbCreatePixKeyRequest
import br.com.zup.edu.chave.bcb.BcbDeletePixKeyRequest
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.exceptions.*
import br.com.zup.edu.chave.exceptions.handlers.ExceptionInterceptor
import br.com.zup.edu.chave.extensions.*
import br.com.zup.edu.chave.validation.PixValidator
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
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
        val detalhes = request.validateDadosClientes(client)
        request.validate(validators)
        request.validateUniqueness(repository)

        try {
            val bcbRequest = BcbCreatePixKeyRequest.fromDetalhesCliente(detalhes, request)
            val bcbResponse = bcbClient.cadastra(bcbRequest)

            val chave = request.toModel(detalhes, bcbResponse.key)
            repository.save(chave)

            val response = CreateKeyResponse.newBuilder()
                .setId(chave.id)
                .build()

            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: HttpClientResponseException) {
            when (e.status) {
                HttpStatus.UNPROCESSABLE_ENTITY -> throw PixAlreadyExistsException()
                HttpStatus.BAD_REQUEST -> throw PixValidationException(e.localizedMessage)
                else -> throw e
            }
        }
    }

    @ExceptionInterceptor
    override fun delete(request: DeleteKeyRequest, responseObserver: StreamObserver<DeleteKeyResponse>) {
        val dados = request.validaCliente(client)

        val chave = repository.findById(request.id).orElseThrow { PixChaveNotFound() }
        if (!request.isDono(chave, dados)) throw PixClientKeyPermissionException()

        try {
            val bcbRequest = BcbDeletePixKeyRequest(chave.chave)
            bcbClient.deleta(chave.chave, bcbRequest)
            repository.delete(chave)

            // Se eventualmente o retorno mudar, temos um builder aqui
            val response = DeleteKeyResponse.newBuilder().build()

            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: HttpClientResponseException) {
            when (e.status) {
                HttpStatus.NOT_FOUND -> throw PixChaveNotFound()
                HttpStatus.FORBIDDEN -> throw PixPermissionDeniedException()
                else -> throw e
            }
        }
    }
}