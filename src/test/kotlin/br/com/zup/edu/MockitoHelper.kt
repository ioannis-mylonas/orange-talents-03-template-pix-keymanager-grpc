package br.com.zup.edu

import org.mockito.Mockito

object MockitoHelper {
    fun <T> anyObject(): T {
        Mockito.any<T>()
        return uninitialized()
    }
    @Suppress("UNCHECKED_CAST")
    fun <T> uninitialized(): T =  null as T

    fun <T> eq(obj: T): T = Mockito.eq<T>(obj)
}