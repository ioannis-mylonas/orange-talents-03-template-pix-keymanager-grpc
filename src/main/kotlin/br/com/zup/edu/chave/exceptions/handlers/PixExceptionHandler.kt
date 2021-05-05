package br.com.zup.edu.chave.exceptions.handlers

import br.com.zup.edu.chave.exceptions.PixException
import io.grpc.Status

/**
 * Handler que recebe e processa um tipo de PixException. Não deve existir
 * mais de um handler para o mesmo tipo de PixException.
 */
interface PixExceptionHandler<E: PixException> {
    /**
     * Retorna um status baseado na PixException passada.
     * @param e A PixException a ser processada.
     * @return Status apropriado, conforme processamento da PixException recebida.
     */
    fun handle(e: E): Status

    /**
     * Verifica suporte à PixException recebida.
     * @param e PixException a ser verificada.
     * @return True se ele puder processar a PixException, false caso contrário.
     */
    fun supports(e: PixException): Boolean
}