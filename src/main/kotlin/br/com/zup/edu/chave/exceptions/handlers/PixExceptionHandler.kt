package br.com.zup.edu.chave.exceptions.handlers

import io.grpc.Status

interface PixExceptionHandler<E: RuntimeException> {
    fun handle(e: E): Status
    fun supports(e: RuntimeException): Boolean
}