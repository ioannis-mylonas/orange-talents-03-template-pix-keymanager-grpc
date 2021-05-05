package br.com.zup.edu.chave.exceptions

/**
 * PixException base para PixExceptions específicas.
 * @param message Mensagem que será mostrada para o usuário.
 */
abstract class PixException(message: String? = null): RuntimeException(message)