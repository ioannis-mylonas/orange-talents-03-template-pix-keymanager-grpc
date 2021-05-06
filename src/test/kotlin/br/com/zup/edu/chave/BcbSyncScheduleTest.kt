package br.com.zup.edu.chave

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.chave.bcb.*
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false, rollback = false)
internal class BcbSyncScheduleTest {
    @Inject lateinit var repository: ChavePixRepository
    @Inject lateinit var bcbSyncSchedule: BcbSyncSchedule
    @Inject lateinit var bcbClient: BcbClient

    @AfterEach
    fun teardown() {
        repository.deleteAll()
    }

    @Test
    fun `Adiciona chaves nao existentes no banco de dados`() {
        val account = BcbBankAccount("BRANCH", "NUMBER", BcbAccountType.CACC)
        val owner = BcbOwner(BcbOwnerType.NATURAL_PERSON, "NAME", "CPF")

        val list = BcbKeyDetailsList(listOf(
            BcbKeyDetails(TipoChave.RANDOM,
                UUID.randomUUID().toString(),
                account, owner, LocalDateTime.now()),
            BcbKeyDetails(TipoChave.RANDOM,
                UUID.randomUUID().toString(),
                account, owner, LocalDateTime.now()),
            BcbKeyDetails(TipoChave.RANDOM,
                UUID.randomUUID().toString(),
                account, owner, LocalDateTime.now())
        ))

        assertEquals(0L, repository.count())

        Mockito.`when`(bcbClient.lista()).thenReturn(list)
        bcbSyncSchedule.sync()

        assertEquals(3L, repository.count())
        bcbSyncSchedule.sync()
        assertEquals(3L, repository.count())
    }

    @Test
    fun `remove chaves existentes do banco de dados`() {
        val chave = ChavePix(TipoChave.RANDOM,
            UUID.randomUUID().toString(),
            TipoConta.CONTA_CORRENTE,
            UUID.randomUUID().toString())

        repository.save(chave)
        assertEquals(1L, repository.count())

        Mockito.`when`(bcbClient.lista()).thenReturn(BcbKeyDetailsList(listOf()))
        bcbSyncSchedule.sync()

        assertEquals(0L, repository.count())
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }
}