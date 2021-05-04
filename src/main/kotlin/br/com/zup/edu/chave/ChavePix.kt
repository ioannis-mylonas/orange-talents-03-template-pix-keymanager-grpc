package br.com.zup.edu.chave

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.constraints.Unique
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
data class ChavePix(
    @field:NotBlank val numero: String,
    @field:Enumerated(EnumType.STRING) @field:NotNull val tipoChave: TipoChave,
    @field:NotBlank @field:Size(max = 77) @field:Unique(field = "chave", target = ChavePix::class) val chave: String,
    @field:Enumerated(EnumType.STRING) @field:NotNull val tipoConta: TipoConta
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}