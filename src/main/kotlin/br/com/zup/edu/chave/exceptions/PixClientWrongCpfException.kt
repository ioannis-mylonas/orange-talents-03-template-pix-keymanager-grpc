package br.com.zup.edu.chave.exceptions

/**
 * Cpf informado não bate com o CPF cadastrado no ERP.
 */
class PixClientWrongCpfException:
    PixPermissionDeniedException("CPF não corresponde ao usuário cadastrado")