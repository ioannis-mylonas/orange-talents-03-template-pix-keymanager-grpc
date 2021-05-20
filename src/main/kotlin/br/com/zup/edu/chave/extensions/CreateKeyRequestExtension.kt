package br.com.zup.edu.chave.extensions

import br.com.zup.edu.CreateKeyRequest
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.chave.ChavePixRepository
import br.com.zup.edu.chave.cliente.ClienteDetalhes
import br.com.zup.edu.chave.exceptions.*
import br.com.zup.edu.chave.validation.PixValidator

/**
 * Valida a chave PIX conforme uma Collection de PixValidators.
 */
fun CreateKeyRequest.valida(validators: Collection<PixValidator>) {
    validators.forEach {
        it.validate(chave, tipoChave)
    }
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
 * Valida dados do cliente conforme ERP.
 * @param detalhes Detalhes do cliente a serem validados.
 * @throws PixPermissionDeniedException Se o CPF não bater com o cadastro no ERP.
 */
fun CreateKeyRequest.validaDadosClientes(detalhes: ClienteDetalhes) {
    if (tipoConta == TipoConta.TIPO_CONTA_DESCONHECIDO || tipoConta == TipoConta.UNRECOGNIZED)
        throw PixTipoContaUnknownException()

    if (tipoChave == TipoChave.CPF && chave != detalhes.titular.cpf) {
        throw PixClientWrongCpfException()
    }
}