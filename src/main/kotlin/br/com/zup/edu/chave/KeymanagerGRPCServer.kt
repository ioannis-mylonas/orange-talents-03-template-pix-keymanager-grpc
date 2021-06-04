package br.com.zup.edu.chave

import br.com.zup.edu.*
import br.com.zup.edu.chave.bcb.BcbClient
import br.com.zup.edu.chave.cliente.BuscaClienteDetalhes
import br.com.zup.edu.chave.cliente.ClienteDetalhes
import br.com.zup.edu.chave.cliente.ClienteDetalhesTitular
import br.com.zup.edu.chave.exceptions.*
import br.com.zup.edu.chave.exceptions.handlers.ExceptionHandlerResolver
import br.com.zup.edu.chave.exceptions.handlers.ExceptionInterceptor
import br.com.zup.edu.chave.extensions.*
import br.com.zup.edu.chave.validation.PixValidator
import io.grpc.Status
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Open
class KeymanagerGRPCServer(
    @Inject val validators: Collection<PixValidator>,
    @Inject val repository: ChavePixRepository,
    @Inject val buscaCliente: BuscaClienteDetalhes,
    @Inject val bcbClient: BcbClient,
    @Inject val handlerResolver: ExceptionHandlerResolver,
    @Inject val buscaChavePix: BuscaChavePix
): KeymanagerGRPCServiceGrpc.KeymanagerGRPCServiceImplBase() {
    private fun<T> success(response: T, observer: StreamObserver<T>) {
        observer.onNext(response)
        observer.onCompleted()
    }

    private fun<T> error(e: Throwable, observer: StreamObserver<T>) {
        if (e !is PixException) {
            observer.onError(Status.UNKNOWN.asRuntimeException())
            throw e
        }

        val handler = handlerResolver.resolve(e)
        val status = handler?.handle(e)
        observer.onError(status)
    }

    @ExceptionInterceptor
    override fun cria(request: CreateKeyRequest, responseObserver: StreamObserver<CreateKeyResponse>) {
        val singleChave = request.validaChave(validators, repository)
        val singleDetalhes = request.getDadosClientes(buscaCliente)

        Single.zip(singleChave, singleDetalhes) { _, detalhes ->
            val chave = repository.saveBcb(request, detalhes, bcbClient)

            CreateKeyResponse.newBuilder()
                .setIdPix(chave.id)
                .build()
        }.subscribeOn(Schedulers.io())
            .subscribe({ success(it, responseObserver) }, { error(it, responseObserver) })
    }

    @ExceptionInterceptor
    override fun delete(request: DeleteKeyRequest, responseObserver: StreamObserver<DeleteKeyResponse>) {
        val singleDetalhes = buscaCliente.asyncBuscaTitular(request.idCliente)

        val singleChave = Single.fromCallable {
            repository.findById(request.idPix).orElseThrow { PixChaveNotFound() }
        }.subscribeOn(Schedulers.newThread()).onErrorResumeNext { Single.error(it) }

        Single.zip(singleChave, singleDetalhes) { chave, dados ->
            request.validaDono(chave, dados)
            repository.deleteBcb(chave, bcbClient)

            // Se eventualmente o retorno mudar, temos um builder aqui
            DeleteKeyResponse.newBuilder().build()
        }.subscribeOn(Schedulers.io())
            .subscribe ({ success(it, responseObserver) }, { error(it, responseObserver) })
    }

    @ExceptionInterceptor
    override fun get(request: GetKeyRequest, responseObserver: StreamObserver<GetKeyResponse>) {
        if(request.chavePix.isNullOrBlank()) buscaChavePix.buscaInterna(request, responseObserver)
        else buscaChavePix.buscaExterna(request, responseObserver)
    }

    @ExceptionInterceptor
    override fun list(request: ListKeyRequest, responseObserver: StreamObserver<ListKeyResponse>) {
        val titular = buscaCliente.buscaTitular(request.idCliente)
        val chaves = repository.findByIdCliente(titular.id)

        val result = mutableSetOf<GetKeyResponse>()
        val operations = mutableListOf<Single<Unit>>()

        chaves.forEach { chave ->
            operations.add(Single.create<Unit> {
                if (repository.validaBcb(chave, bcbClient)) {
                    val detalhes = buscaCliente.buscaDetalhesCliente(request.idCliente, chave.tipoConta)

                    val resTitular = GetKeyResponse.Titular.newBuilder()
                        .setNome(titular.nome)
                        .setCpf(titular.cpf)
                        .build()

                    val resInstituicao = GetKeyResponse.Instituicao.newBuilder()
                        .setNome(detalhes.instituicao.nome)
                        .setAgencia(detalhes.agencia)
                        .setConta(detalhes.numero)
                        .setTipoConta(detalhes.tipo)
                        .build()

                    result.add(
                        GetKeyResponse.newBuilder()
                            .setIdPix(chave.id)
                            .setIdCliente(request.idCliente)
                            .setTipoChave(chave.tipoChave)
                            .setChave(chave.chave)
                            .setTitular(resTitular)
                            .setInstituicao(resInstituicao)
                            .setCriadaEm(chave.dataCriacao.toString())
                            .build()
                    )
                }

                it.onSuccess(Unit)
            }.subscribeOn(Schedulers.newThread()).onErrorResumeNext { Single.error(it) })
        }

        if (operations.isEmpty()) {
            val response = ListKeyResponse.getDefaultInstance()
            success(response, responseObserver)
        } else {
            Single.zip(operations) {
                ListKeyResponse.newBuilder().addAllChaves(result).build()
            }.subscribe({ success(it, responseObserver) }, { error(it, responseObserver) })
        }
    }
}