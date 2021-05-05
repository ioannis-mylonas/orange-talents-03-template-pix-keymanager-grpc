package br.com.zup.edu.chave.exceptions

/**
 * Cliente não tem permissão para manipular essa chave PIX.
 */
class PixClientKeyPermissionException:
    PixPermissionDeniedException("Você não tem permissão para acessar essa chave")