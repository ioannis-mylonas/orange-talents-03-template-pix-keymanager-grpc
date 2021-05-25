package br.com.zup.edu.chave.extensions

import br.com.zup.edu.CreateKeyRequest
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.chave.ChavePixRepository
import br.com.zup.edu.chave.cliente.BuscaClienteDetalhes
import br.com.zup.edu.chave.cliente.ClienteDetalhes
import br.com.zup.edu.chave.exceptions.*
import br.com.zup.edu.chave.validation.PixValidator
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * Valida de forma assíncrona a chave PIX conforme uma Collection
 * de PixValidators e seus métodos validadores internos.
 * @param validators Validadores a serem utilizados
 * @param repository Repositório de chave PIX
 * @return Single que irá validar a chave PIX
 */
fun CreateKeyRequest.validaChave(validators: Collection<PixValidator>,
                                 repository: ChavePixRepository): Single<Unit> {
    return Single.create<Unit> {
        validators.forEach { validator ->
            validator.validate(chave, tipoChave)
        }

        validaUniqueness(repository)
        it.onSuccess(Unit)
    }.onErrorResumeNext { Single.error(it) }.subscribeOn(Schedulers.newThread())
}

/**
 * Valida se a chave PIX especificada é única.
 * @throws PixAlreadyExistsException Se a chave já existir no banco de dados.
 */
fun CreateKeyRequest.validaUniqueness(repository: ChavePixRepository) {
    if (repository.existsByChave(chave)) {
        throw PixAlreadyExistsException()
    }
}

/**
 * Busca os dados do cliente conforme a request, de forma assíncrona.
 * @param buscaCliente Bean para busca de clientes
 * @return Single que irá buscar os dados do cliente
 */
fun CreateKeyRequest.getDadosClientes(buscaCliente: BuscaClienteDetalhes): Single<ClienteDetalhes> {
    return Single.create<ClienteDetalhes> {
        val detalhes = buscaCliente.buscaDetalhesCliente(idCliente, tipoConta)
        validaDadosClientes(detalhes)
        it.onSuccess(detalhes)
    }.onErrorResumeNext { Single.error(it) }.subscribeOn(Schedulers.newThread())
}

/**
 * Valida dados do cliente conforme ERP.
 * @param detalhes Detalhes do cliente a serem validados.
 * @throws PixPermissionDeniedException Se o CPF não bater com o cadastro no ERP.
 */
fun CreateKeyRequest.validaDadosClientes(detalhes: ClienteDetalhes): ClienteDetalhes {
    if (tipoConta == TipoConta.TIPO_CONTA_DESCONHECIDO || tipoConta == TipoConta.UNRECOGNIZED)
        throw PixTipoContaUnknownException()

    if (tipoChave == TipoChave.CPF && chave != detalhes.titular.cpf) {
        throw PixClientWrongCpfException()
    }

    return detalhes
}