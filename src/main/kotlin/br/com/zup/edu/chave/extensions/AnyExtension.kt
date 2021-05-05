package br.com.zup.edu.chave.extensions

import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Usa o Jackson para transformar esse objeto em um JSON
 * @return String JSON que representa o objeto
 */
fun Any.toJson(): String {
    return ObjectMapper().writeValueAsString(this)
}