package br.com.zup.edu.chave.validation

import br.com.zup.edu.Open
import br.com.zup.edu.TipoChave
import io.micronaut.validation.validator.Validator
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@Singleton
@Open
class CelularPixValidator: PixValidator {
    override fun shouldValidate(chave: String?, tipo: TipoChave): Boolean {
        return tipo == TipoChave.PHONE
    }

    override fun check(@Pattern(regexp = "^\\+[1-9][0-9]\\d{1,14}\$", message = "Formato de celular inválido.")
                       @NotBlank @Size(max = 77) chave: String?) {}
}