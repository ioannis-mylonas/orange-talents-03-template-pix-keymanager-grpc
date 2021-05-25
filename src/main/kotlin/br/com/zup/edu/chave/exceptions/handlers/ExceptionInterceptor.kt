package br.com.zup.edu.chave.exceptions.handlers

import br.com.zup.edu.chave.exceptions.PixException
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import io.micronaut.aop.Around
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*

/**
 * Marca o método para ser interceptado em busca de PixExceptions.
 */
@Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
@Retention(RUNTIME)
@Around
annotation class ExceptionInterceptor()

@Singleton
@InterceptorBean(ExceptionInterceptor::class)
private class ExceptionInterceptorResolver(@Inject private val resolver: ExceptionHandlerResolver): MethodInterceptor<Any, Any> {
    private fun handle(e: PixException, observer: StreamObserver<*>?) {
        val handler = resolver.resolve(e) ?: return
        val status = handler.handle(e)
        observer?.onError(status)
    }

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {
        try {
            return context.proceed()
        } catch (e: PixException) {
            val observer = context
                .parameterValues
                .filter { it is StreamObserver<*> }
                .firstOrNull() as StreamObserver<*>

            handle(e, observer)
            throw e
        }
    }
}