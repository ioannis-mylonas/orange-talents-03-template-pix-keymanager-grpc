package br.com.zup.edu.chave.exceptions

/**
 * Chave Pix já cadastrada no banco de dados, não deve ser cadastrada
 * novamente.
 */
class PixAlreadyExistsException: PixException("Chave PIX já cadastrada.")