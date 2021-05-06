package br.com.zup.edu.chave.extensions

import br.com.zup.edu.CreateKeyRequest
import br.com.zup.edu.TipoChave
import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.chave.ChavePixRepository
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.cliente.ClienteDetalhes
import br.com.zup.edu.chave.exceptions.PixAlreadyExistsException
import br.com.zup.edu.chave.exceptions.PixClientNotFoundException
import br.com.zup.edu.chave.exceptions.PixPermissionDeniedException
import br.com.zup.edu.chave.validation.PixValidator
import io.micronaut.http.client.exceptions.HttpClientResponseException
import java.util.*

/**
 * Converte a request para o model, para ser persistido no banco de dados.
 * @return ChavePix com os dados especificados, pronta para ser persistida.
 */
fun CreateKeyRequest.toModel(detalhes: ClienteDetalhes): ChavePix {
    val key = if (chave.isNullOrBlank()) UUID.randomUUID().toString() else chave
    return ChavePix(tipoChave, key, tipoConta, detalhes.titular.cpf)
}

/**
 * Valida a chave PIX conforme uma Collection de PixValidators.
 */
fun CreateKeyRequest.validate(validators: Collection<PixValidator>) {
    validators.forEach {
        it.validate(chave, tipoChave)
    }
}

/**
 * Valida se a chave PIX especificada é única.
 * @throws PixAlreadyExistsException Se a chave já existir no banco de dados.
 */
fun CreateKeyRequest.validateUniqueness(repository: ChavePixRepository) {
    if (repository.existsByChave(chave)) {
        throw PixAlreadyExistsException()
    }
}

/**
 * Valida dados do cliente conforme ERP.
 * @param client Uma instância de HTTP client que possa fazer a requisição ao ERP.
 * @throws PixClientNotFoundException Se o número ou tipo de conta não for encontrado.
 * @throws PixPermissionDeniedException Se o CPF não bater com o cadastro no ERP.
 */
fun CreateKeyRequest.validateDadosClientes(client: ChaveClient): ClienteDetalhes {
    val detalhes = try {
        client.buscaDetalhes(numero, tipoConta)
    } catch (e: HttpClientResponseException) {
        throw PixClientNotFoundException(numero, tipoConta)
    } ?: throw PixClientNotFoundException(numero, tipoConta)

    if (tipoChave == TipoChave.CPF && chave != detalhes.titular.cpf) {
        throw PixPermissionDeniedException()
    }

    return detalhes
}