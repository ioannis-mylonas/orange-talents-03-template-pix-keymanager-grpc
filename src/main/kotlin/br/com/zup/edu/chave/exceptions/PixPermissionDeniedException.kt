package br.com.zup.edu.chave.exceptions

/**
 * PixException que retorna Status.PERMISSION_DENIED.
 * @param message Mensagem que será mostrada para o usuário.
 */
open class PixPermissionDeniedException(message: String? = null): PixException(message)