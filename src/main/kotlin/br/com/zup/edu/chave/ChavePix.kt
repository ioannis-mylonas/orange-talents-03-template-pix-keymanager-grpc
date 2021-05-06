package br.com.zup.edu.chave

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
data class ChavePix(
    @field:Enumerated(EnumType.STRING) @field:NotNull val tipoChave: TipoChave,
    @field:NotBlank @field:Size(max = 77) val chave: String,
    @field:Enumerated(EnumType.STRING) @field:NotNull val tipoConta: TipoConta,
    @field:NotBlank val cpf: String
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}