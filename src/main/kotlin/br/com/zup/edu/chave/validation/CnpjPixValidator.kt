package br.com.zup.edu.chave.validation

import br.com.zup.edu.Open
import br.com.zup.edu.TipoChave
import javax.inject.Singleton
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@Singleton
@Open
class CnpjPixValidator: PixValidator {
    override fun shouldValidate(chave: String?, tipo: TipoChave): Boolean {
        return tipo == TipoChave.CNPJ
    }

    override fun check(@Pattern(regexp = "^[0-9]{14}\$", message = "Formato de CNPJ inválido")
                       @Size(max = 77) @NotBlank chave: String?) {}
}