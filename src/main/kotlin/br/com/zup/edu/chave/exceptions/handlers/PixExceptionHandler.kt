package br.com.zup.edu.chave.exceptions.handlers

import br.com.zup.edu.chave.exceptions.PixException
import com.google.rpc.Code
import com.google.rpc.Status

/**
 * Handler que recebe e processa um tipo de PixException. Não deve existir
 * mais de um handler para o mesmo tipo de PixException.
 */
abstract class PixExceptionHandler<E: PixException> {
    /**
     * Retorna um status baseado na PixException passada.
     * @param e A PixException a ser processada.
     * @return Status apropriado, conforme processamento da PixException recebida.
     */
    abstract fun handle(e: E): Status

    protected fun handle(e: E, code: Int): Status {
        val builder = Status.newBuilder().setCode(code)
        if (e.message != null) builder.setMessage(e.message)
        return builder.build()
    }

    /**
     * Verifica suporte à PixException recebida.
     * @param e PixException a ser verificada.
     * @return True se ele puder processar a PixException, false caso contrário.
     */
    abstract fun supports(e: PixException): Boolean
}