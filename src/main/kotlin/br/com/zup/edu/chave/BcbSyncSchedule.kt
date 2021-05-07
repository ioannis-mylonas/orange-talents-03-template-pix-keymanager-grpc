package br.com.zup.edu.chave

import br.com.zup.edu.chave.bcb.BcbClient
import br.com.zup.edu.chave.bcb.BcbKeyDetailsList
import io.micronaut.scheduling.annotation.Scheduled
import org.jboss.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BcbSyncSchedule(
    @Inject val bcbClient: BcbClient,
    @Inject val repository: ChavePixRepository
) {
    val logger = Logger.getLogger(BcbSyncSchedule::class.java)

    @Scheduled(fixedDelay = "10s")
    fun sync() {
        logger.info("Syncing database to BCB...")
        val list = bcbClient.lista()

        // Adiciona chaves existentes no BCB
        addKeys(list)

        // Remove chaves inexistentes no BCB
        removeKeys(list)
    }

    /**
     * Adiciona as chaves existentes no BCB que não foram adicionadas
     * ao nosso banco de dados.
     * @param list Lista recebida pela consulta ao BCB
     */
    private fun addKeys(list: BcbKeyDetailsList) {
        list.pixKeys.forEach {
            val key = repository.findByChave(it.key)

            if (key.isEmpty) {
                val target = ChavePix(it.keyType,
                    it.key, it.bankAccount.accountType.toTipoConta(), it.owner.taxIdNumber, it.createdAt)
                repository.save(target)
            }
        }
    }

    private fun removeKeys(list: BcbKeyDetailsList) {
        val repoList = repository.findAll()

        repoList.forEach { key ->
            val target = list.pixKeys.find { key.chave == it.key }
            if (target == null) repository.deleteByChave(key.chave)
        }
    }
}