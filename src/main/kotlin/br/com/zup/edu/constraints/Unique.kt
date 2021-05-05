package br.com.zup.edu.constraints

import br.com.zup.edu.Open
import io.micronaut.core.annotation.AnnotationClassValue
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.transaction.Transactional
import javax.validation.Constraint
import kotlin.reflect.KClass

/**
 * Validador que espera que o valor seja único no banco de dados,
 * conforme coluna e tipo especificados.
 * @param field Campo/coluna que deve ter valor único
 * @param target Classe/tabela a ser buscada no banco de dados
 */
@Constraint(validatedBy = [ UniqueValidator::class ])
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Unique(
    val message: String = "Valor deve ser único.",
    val field: String,
    val target: KClass<*>
)

@Singleton
@Open
class UniqueValidator(private val entityManager: EntityManager): ConstraintValidator<Unique, Any> {
    @Transactional
    override fun isValid(
        value: Any?,
        annotationMetadata: AnnotationValue<Unique>,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value == null) return true

        val field = annotationMetadata.get("field", String::class.java).get()
        val target = annotationMetadata.get("target", AnnotationClassValue::class.java).get()

        val q = "SELECT 1 FROM ${target.name} WHERE $field = :value"
        val query = entityManager.createQuery(q)
        query.setParameter("value", value)

        return query.resultList.isEmpty()
    }
}
