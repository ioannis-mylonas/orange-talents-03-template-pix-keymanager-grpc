package br.com.zup.edu.chave.exceptions

/**
 * Chave PIX não existe no banco de dados.
 */
class PixChaveNotFound: PixNotFoundException("Chave PIX não encontrada.")