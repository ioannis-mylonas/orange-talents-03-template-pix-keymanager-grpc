package br.com.zup.edu.chave.extensions

import br.com.zup.edu.CreateKeyRequest
import br.com.zup.edu.CreateKeyResponse
import br.com.zup.edu.TipoChave
import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.chave.ChavePixRepository
import br.com.zup.edu.chave.cliente.ChaveClient
import br.com.zup.edu.chave.validation.PixValidator
import io.grpc.Status
import io.grpc.stub.StreamObserver
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
fun CreateKeyRequest.validate(validators: Collection<PixValidator>, observer: StreamObserver<CreateKeyResponse>): Boolean {
    val errors = mutableSetOf<ConstraintViolation<*>>()
    validators.forEach {
        errors.addAll(it.validate(chave, tipoChave))
    }

    if (errors.isNotEmpty()) {
        var status = Status.INVALID_ARGUMENT.withDescription("Pedido inválido.")
        errors.forEach { status = status.augmentDescription(it.message) }
        observer.onError(status.asRuntimeException())
        return false
    }
    return true
}

/**
 * Valida se a chave PIX especificada é única. Caso falhe, processamento deve ser parado imediatamente.
 * @return True se for única, false caso contrário.
 */
fun CreateKeyRequest.validateUniqueness(repository: ChavePixRepository, observer: StreamObserver<CreateKeyResponse>): Boolean {
    if (repository.existsByChave(chave)) {
        val status = Status.ALREADY_EXISTS
            .withDescription("Chave já cadastrada.")
            .asRuntimeException()

        observer.onError(status)
        return false
    }
    return true
}

/**
 * Valida dados do cliente conforme ERP. Caso algum erro seja encontrado, o processamento deve parar imediatamente.
 * @return True se os dados são válidos. False caso algum erro seja encontrado.
 */
fun CreateKeyRequest.validateDadosClientes(client: ChaveClient, observer: StreamObserver<CreateKeyResponse>): Boolean {
    val detalhes = client.buscaDetalhes(numero, tipoConta)
    if (detalhes == null) {
        val status = Status.NOT_FOUND
            .withDescription("Conta não encontrada no sistema.")
            .asRuntimeException()

        observer.onError(status)
        return false
    }

    if (tipoChave == TipoChave.CPF && chave != detalhes.titular.cpf) {
        val status = Status.PERMISSION_DENIED
            .withDescription("CPF não corresponde ao usuário cadastrado")
            .asRuntimeException()

        observer.onError(status)
        return false
    }

    return true
}