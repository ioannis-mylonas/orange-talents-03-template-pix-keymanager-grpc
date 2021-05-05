package br.com.zup.edu.chave.exceptions.handlers

import br.com.zup.edu.chave.exceptions.PixException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Busca o handler apropriado para a PixException recebida.
 */
@Singleton
class ExceptionHandlerResolver(@Inject private val handlers: Collection<PixExceptionHandler<PixException>>) {
    /**
     * Devolve o handler apropriado para a PixException passada.
     * @param e A PixException que deve ser resolvida a um handler.
     * @return O handler apropriado, ou null se nenhum estiver disponível.
     * @throws IllegalStateException Se existir mais de um handler disponível para a PixException
     */
    fun resolve(e: PixException): PixExceptionHandler<PixException>? {
        val result = handlers.filter { it.supports(e) }
        if (result.size > 1) throw IllegalStateException("Você criou mais de um handler suportando a exception ${e.javaClass.name}: $result")
        return result.firstOrNull()
    }
}