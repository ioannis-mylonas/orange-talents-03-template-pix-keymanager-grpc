package br.com.zup.edu.chave.exceptions.handlers

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionHandlerResolver(@Inject private val handlers: Collection<PixExceptionHandler<RuntimeException>>) {
    fun resolve(e: RuntimeException): PixExceptionHandler<RuntimeException>? {
        val result = handlers.filter { it.supports(e) }
        if (result.size > 1) throw IllegalStateException("Você criou mais de um handler suportando a exception ${e.javaClass.name}: $result")
        return result.firstOrNull()
    }
}