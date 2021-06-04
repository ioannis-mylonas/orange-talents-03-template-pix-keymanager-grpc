package br.com.zup.edu.chave

import br.com.zup.edu.GetKeyRequest
import br.com.zup.edu.GetKeyResponse
import br.com.zup.edu.chave.bcb.BcbClient
import br.com.zup.edu.chave.cliente.BuscaClienteDetalhes
import br.com.zup.edu.chave.cliente.ClienteDetalhes
import br.com.zup.edu.chave.exceptions.PixChaveNotFound
import br.com.zup.edu.chave.exceptions.PixException
import br.com.zup.edu.chave.exceptions.handlers.ExceptionHandlerResolver
import br.com.zup.edu.chave.extensions.validaDono
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuscaChavePix {
    @Inject lateinit var repository: ChavePixRepository
    @Inject lateinit var bcbClient: BcbClient
    @Inject lateinit var buscaCliente: BuscaClienteDetalhes
    @Inject lateinit var handlerResolver: ExceptionHandlerResolver

    private fun getTitular(detalhes: ClienteDetalhes): Single<GetKeyResponse.Titular> {
        return Single.fromCallable {
            GetKeyResponse.Titular.newBuilder()
                .setCpf(detalhes.titular.cpf)
                .setNome(detalhes.titular.nome)
                .build()
        }.subscribeOn(Schedulers.newThread()).onErrorResumeNext { Single.error(it) }
    }

    private fun getInstituicao(detalhes: ClienteDetalhes): Single<GetKeyResponse.Instituicao> {
        return Single.fromCallable {
            GetKeyResponse.Instituicao.newBuilder()
                .setAgencia(detalhes.agencia)
                .setConta(detalhes.numero)
                .setNome(detalhes.instituicao.nome)
                .setTipoConta(detalhes.tipo)
                .build()
        }.subscribeOn(Schedulers.newThread()).onErrorResumeNext { Single.error(it) }
    }

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

    private fun respond(
        chave: ChavePix,
        titularSingle: Single<GetKeyResponse.Titular>,
        instituicaoSingle: Single<GetKeyResponse.Instituicao>,
        responseObserver: StreamObserver<GetKeyResponse>
    ) {
        Single.zip(titularSingle, instituicaoSingle) { titular, instituicao ->
            GetKeyResponse.newBuilder()
                .setIdPix(chave.id)
                .setChave(chave.chave)
                .setTipoChave(chave.tipoChave)
                .setIdCliente(chave.idCliente)
                .setTitular(titular)
                .setInstituicao(instituicao)
                .setCriadaEm(chave.dataCriacao.toString())
                .build()
        }.subscribe({ success(it, responseObserver) }, { error(it, responseObserver) })
    }

    fun buscaExterna(request: GetKeyRequest, responseObserver: StreamObserver<GetKeyResponse>) {
        val chave = repository.findByChave(request.chavePix).orElseGet buscaBcb@ {
            try {
                val bcbChave = bcbClient.get(request.chavePix) ?: throw PixChaveNotFound()
                return@buscaBcb bcbChave.toChavePix()
            } catch (e: HttpClientResponseException) {
                throw PixChaveNotFound()
            }
        }

        val detalhes = buscaCliente.buscaDetalhesCliente(chave.idCliente, chave.tipoConta)

        val titularSingle = getTitular(detalhes)
        val instituicaoSingle = getInstituicao(detalhes)

        respond(chave, titularSingle, instituicaoSingle, responseObserver)
    }

    fun buscaInterna(request: GetKeyRequest, responseObserver: StreamObserver<GetKeyResponse>) {
        val chave = repository.findBcb(request.idPix, bcbClient)
        val detalhes = buscaCliente.buscaDetalhesCliente(request.idCliente, chave.tipoConta)

        request.validaDono(chave, detalhes.titular)

        val titularSingle = getTitular(detalhes)
        val instituicaoSingle = getInstituicao(detalhes)

        respond(chave, titularSingle, instituicaoSingle, responseObserver)
    }
}