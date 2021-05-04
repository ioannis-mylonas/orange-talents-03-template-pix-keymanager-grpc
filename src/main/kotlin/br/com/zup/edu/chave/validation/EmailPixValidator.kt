package br.com.zup.edu.chave.validation

import br.com.zup.edu.Open
import br.com.zup.edu.TipoChave
import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.constraints.Unique
import io.micronaut.validation.validator.Validator
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolation
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Singleton
@Open
class EmailPixValidator(@Inject val validator: Validator): PixValidator {
    override fun shouldValidate(chave: String?, tipo: TipoChave): Boolean {
        return tipo == TipoChave.EMAIL
    }

    override fun check(@Email @Size(max = 77)
                       @NotBlank @Unique(field = "chave", target = ChavePix::class)
                       chave: String?) {}
}