package br.com.zup.edu.chave

import br.com.zup.edu.CreateKeyRequest
import br.com.zup.edu.DeleteKeyRequest
import br.com.zup.edu.chave.bcb.*
import br.com.zup.edu.chave.cliente.ClienteDetalhes
import br.com.zup.edu.chave.exceptions.PixAlreadyExistsException
import br.com.zup.edu.chave.exceptions.PixChaveNotFound
import br.com.zup.edu.chave.exceptions.PixPermissionDeniedException
import br.com.zup.edu.chave.exceptions.PixValidationException
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import java.net.http.HttpClient
import java.util.*
import javax.transaction.Transactional

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, Long> {
    fun existsByChave(chave: String): Boolean
    fun findByChave(chave: String): Optional<ChavePix>
    fun deleteByChave(chave: String)
    fun findByCpf(cpf: String): Collection<ChavePix>
}

/**
 * Salva a chave no BCB, e no nosso sistema se conseguir salvar no BCB.
 * @param input Request vindo do usuário.
 * @param detalhes Detalhes do cliente, consultados no sistema interno.
 * @param bcbClient O client para comunicação com o BCB.
 * @return Chave salva, com ID gerado pelo sistema.
 */
@Transactional
fun ChavePixRepository.saveBcb(input: CreateKeyRequest, detalhes: ClienteDetalhes, bcbClient: BcbClient): ChavePix {
    try {
        val request = BcbCreatePixKeyRequest.fromDetalhesCliente(detalhes, input)
        val response = bcbClient.cadastra(request)

        val chave = ChavePix(response.keyType,
            response.key,
            response.bankAccount.accountType.toTipoConta(),
            response.owner.taxIdNumber,
            response.createdAt)

        return save(chave)
    } catch (e: HttpClientResponseException) {
        when (e.status) {
            HttpStatus.UNPROCESSABLE_ENTITY -> throw PixAlreadyExistsException()
            HttpStatus.BAD_REQUEST -> throw PixValidationException(e.localizedMessage)
            else -> throw e
        }
    }
}

/**
 * Exclui chave no BCB e, se bem sucedido, no nosso sistema.
 * @param chave Chave a ser excluída.
 * @param bcbClient Client para comunicação com BCB.
 */
@Transactional
fun ChavePixRepository.deleteBcb(chave: ChavePix, bcbClient: BcbClient) {
    try {
        val request = BcbDeletePixKeyRequest(chave.chave)
        bcbClient.deleta(chave.chave, request)
        deleteByChave(chave.chave)
    } catch (e: HttpClientResponseException) {
        when (e.status) {
            HttpStatus.NOT_FOUND -> throw PixChaveNotFound()
            HttpStatus.FORBIDDEN -> throw PixPermissionDeniedException()
            else -> throw e
        }
    }
}

/**
 * Retorna a chave se ela estiver também cadastrada no BCB.
 * @param idChave PIX ID.
 * @param bcbClient Client para comunicação com o BCB.
 * @return Chave PIX, existir.
 * @throws PixChaveNotFound Se a chave PIX não existir ou não estiver cadastrada no BCB.
 */
fun ChavePixRepository.findBcb(idChave: Long, bcbClient: BcbClient): ChavePix {
    val chave = findById(idChave).orElseThrow { PixChaveNotFound() }
    if (!validaBcb(chave, bcbClient)) throw PixChaveNotFound()

    return chave
}

/**
 * Valida se a chave está cadastrada no BCB.
 * @param chave Chave a ser validada
 * @param bcbClient Client para comunicação com o BCB.
 * @return True se estiver cadastrada, false caso contrário.
 */
fun ChavePixRepository.validaBcb(chave: ChavePix, bcbClient: BcbClient): Boolean {
    try {
        bcbClient.get(chave.chave) ?: return false
        return true
    } catch (e: HttpClientResponseException) {
        return false
    }
}