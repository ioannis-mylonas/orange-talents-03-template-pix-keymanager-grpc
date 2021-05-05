package br.com.zup.edu.chave.extensions

import br.com.zup.edu.CreateKeyRequest
import br.com.zup.edu.CreateKeyResponse
import br.com.zup.edu.TipoChave
import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.chave.ChavePixRepository
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.exceptions.PixAlreadyExistsException
import br.com.zup.edu.chave.exceptions.PixClientNotFoundException
import br.com.zup.edu.chave.exceptions.PixClientWrongCpfException
import br.com.zup.edu.chave.validation.PixValidator
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientResponseException
import java.util.*
import javax.validation.ConstraintViolation

/**
 * Converte a request para o model, para ser persistido no banco de dados.
 * @return ChavePix com os dados especificados, pronta para ser persistida.
 */
fun CreateKeyRequest.toModel(): ChavePix {
    val key = if (chave.isNullOrBlank()) UUID.randomUUID().toString() else chave
    return ChavePix(numero, tipoChave, key, tipoConta)
}

/**
 * Valida a chave PIX conforme uma Collection de PixValidator.
 * @return True se todas as validações passaram, caso contrário, false.
 */
fun CreateKeyRequest.validate(validators: Collection<PixValidator>) {
    validators.forEach {
        it.validate(chave, tipoChave)
    }
}

/**
 * Valida se a chave PIX especificada é única. Caso falhe, processamento deve ser parado imediatamente.
 * @return True se for única, false caso contrário.
 */
fun CreateKeyRequest.validateUniqueness(repository: ChavePixRepository) {
    if (repository.existsByChave(chave)) {
        throw PixAlreadyExistsException()
    }
}

/**
 * Valida dados do cliente conforme ERP. Caso algum erro seja encontrado, o processamento deve parar imediatamente.
 * @return True se os dados são válidos. False caso algum erro seja encontrado.
 */
fun CreateKeyRequest.validateDadosClientes(client: ChaveClient) {
    val detalhes = try {
        client.buscaDetalhes(numero, tipoConta)
    } catch (e: HttpClientResponseException) {
        throw PixClientNotFoundException(numero, tipoConta)
    } ?: throw PixClientNotFoundException(numero, tipoConta)

    if (tipoChave == TipoChave.CPF && chave != detalhes.titular.cpf) {
        throw PixClientWrongCpfException()
    }
}