package br.com.zup.edu.chave.extensions

import com.fasterxml.jackson.databind.ObjectMapper

fun Any.toJson(): String {
    return ObjectMapper().writeValueAsString(this)
}