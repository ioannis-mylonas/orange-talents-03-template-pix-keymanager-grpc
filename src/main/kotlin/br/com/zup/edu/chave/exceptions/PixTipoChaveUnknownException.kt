package br.com.zup.edu.chave.exceptions

/**
 * Tipo de chave vindo da request é de tipo Unknown.
 */
class PixTipoChaveUnknownException: PixValidationException("Tipo de chave PIX não preenchido.") {
}