package br.com.zup.edu.chave

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false, rollback = false)
internal class ChavePixRepositoryTest {
    @Inject lateinit var repository: ChavePixRepository

    private val valor = UUID.randomUUID().toString()
    private val chave = ChavePix(UUID.randomUUID().toString(),
        TipoChave.ALEATORIO, valor,
        TipoConta.CONTA_CORRENTE)

    @BeforeEach
    fun setup() {
        repository.save(chave)
    }

    @AfterEach
    fun teardown() {
        repository.deleteAll()
    }

    @Test
    fun `testa encontra chave PIX ja existente`() {
        assertTrue(repository.existsByChave(valor))
    }

    @Test
    fun `testa nao encontra chave PIX inexistente`() {
        assertTrue(!repository.existsByChave(UUID.randomUUID().toString()))
    }
}