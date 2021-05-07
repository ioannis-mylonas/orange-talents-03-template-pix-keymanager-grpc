package br.com.zup.edu.chave

import br.com.zup.edu.*
import br.com.zup.edu.chave.bcb.BcbAccountType
import br.com.zup.edu.chave.bcb.BcbClient
import br.com.zup.edu.chave.cliente.BuscaClienteDetalhes
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.exceptions.*
import br.com.zup.edu.chave.exceptions.handlers.ExceptionInterceptor
import br.com.zup.edu.chave.extensions.*
import br.com.zup.edu.chave.validation.PixValidator
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Open
class KeymanagerGRPCServer(
    @Inject val validators: Collection<PixValidator>,
    @Inject val repository: ChavePixRepository,
    @Inject val buscaCliente: BuscaClienteDetalhes,
    @Inject val bcbClient: BcbClient
): KeymanagerGRPCServiceGrpc.KeymanagerGRPCServiceImplBase() {
    @ExceptionInterceptor
    override fun cria(request: CreateKeyRequest, responseObserver: StreamObserver<CreateKeyResponse>) {
        val detalhes = buscaCliente.buscaDetalhesCliente(request.idCliente, request.tipoConta)

        request.validaDadosClientes(detalhes)
        request.valida(validators)
        request.validaUniqueness(repository)

        val chave = repository.saveBcb(request, detalhes, bcbClient)

        val response = CreateKeyResponse.newBuilder()
            .setIdPix(chave.id)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    @ExceptionInterceptor
    override fun delete(request: DeleteKeyRequest, responseObserver: StreamObserver<DeleteKeyResponse>) {
        val dados = buscaCliente.buscaTitular(request.idCliente)

        val chave = repository.findById(request.idPix).orElseThrow { PixChaveNotFound() }
        if (!request.isDono(chave, dados)) throw PixClientKeyPermissionException()

        repository.deleteBcb(chave, bcbClient)

        // Se eventualmente o retorno mudar, temos um builder aqui
        val response = DeleteKeyResponse.newBuilder().build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    @ExceptionInterceptor
    override fun get(request: GetKeyRequest, responseObserver: StreamObserver<GetKeyResponse>) {
        val chave = repository.findBcb(request.idPix, bcbClient)
        val detalhes = buscaCliente.buscaDetalhesCliente(request.idCliente, chave.tipoConta)

        if (!request.isDono(chave, detalhes.titular)) throw PixClientKeyPermissionException()

        val responseTitular = GetKeyResponse.Titular.newBuilder()
            .setCpf(detalhes.titular.cpf)
            .setNome(detalhes.titular.nome)
            .build()

        val responseInstituicao = GetKeyResponse.Instituicao.newBuilder()
            .setAgencia(detalhes.agencia)
            .setConta(detalhes.numero)
            .setNome(detalhes.instituicao.nome)
            .setTipoConta(detalhes.tipo)
            .build()

        val response = GetKeyResponse.newBuilder()
            .setIdPix(chave.id)
            .setChave(chave.chave)
            .setTipoChave(chave.tipoChave)
            .setIdCliente(request.idCliente)
            .setTitular(responseTitular)
            .setInstituicao(responseInstituicao)
            .setCriadaEm(chave.dataCriacao.toString())
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    @ExceptionInterceptor
    override fun list(request: ListKeyRequest, responseObserver: StreamObserver<ListKeyResponse>) {
        val titular = buscaCliente.buscaTitular(request.idCliente)
        val chaves = repository.findByCpf(titular.cpf)

        val result = mutableSetOf<GetKeyResponse>()
        chaves.forEach {
            if (repository.validaBcb(it, bcbClient)) {
                val detalhes = buscaCliente.buscaDetalhesCliente(request.idCliente, it.tipoConta)

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
                        .setIdPix(it.id)
                        .setIdCliente(request.idCliente)
                        .setTipoChave(it.tipoChave)
                        .setChave(it.chave)
                        .setTitular(resTitular)
                        .setInstituicao(resInstituicao)
                        .setCriadaEm(it.dataCriacao.toString())
                        .build()
                )
            }
        }

        val response = ListKeyResponse.newBuilder().addAllChaves(result).build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}