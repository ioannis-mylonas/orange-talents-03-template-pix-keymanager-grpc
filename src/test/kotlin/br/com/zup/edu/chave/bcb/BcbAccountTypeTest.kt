package br.com.zup.edu.chave.bcb

import br.com.zup.edu.TipoConta
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@MicronautTest
internal class BcbAccountTypeTest {
    @Test
    fun `Testa BCB tipo conta corrente para Tipo Conta`() {
        assertEquals(BcbAccountType.CACC.toTipoConta(), TipoConta.CONTA_CORRENTE)
    }

    @Test
    fun `Testa BCB tipo conta poupanca para Tipo Conta`() {
        assertEquals(BcbAccountType.SVGS.toTipoConta(), TipoConta.CONTA_POUPANCA)
    }
}