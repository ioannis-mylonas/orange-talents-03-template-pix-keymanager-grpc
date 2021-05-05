package br.com.zup.edu.chave.exceptions

/**
 * PixException que retorna Status.NOT_FOUND.
 * @param message Mensagem a ser retornada ao usuário.
 */
open class PixNotFoundException(message: String? = null): PixException(message)