package br.com.zup.edu.chave

import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class ChavePix(
    @field:Enumerated(EnumType.STRING) @field:NotNull
    @field:Column(nullable = false)
    val tipoChave: TipoChave,

    @field:NotBlank @field:Size(max = 77)
    @field:Column(nullable = false, unique = true)
    val chave: String,

    @field:Enumerated(EnumType.STRING) @field:NotNull
    @field:Column(nullable = false)
    val tipoConta: TipoConta,

    @field:NotBlank
    @field:Column(nullable = false)
    val cpf: String,

    @field:NotBlank
    @field:Column(nullable = false)
    val dataCriacao: LocalDateTime
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}