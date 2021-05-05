package br.com.zup.edu.constraints

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint

/**
 * Validador que espera o objeto anotado como tendo valor null ou empty.
 */
@Constraint(validatedBy = [ IsEmptyValidator::class ])
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class IsEmpty(
    val message: String = "Valor deve estar vazio."
)

@Singleton
class IsEmptyValidator: ConstraintValidator<IsEmpty, Any> {
    override fun isValid(
        value: Any?,
        annotationMetadata: AnnotationValue<IsEmpty>,
        context: ConstraintValidatorContext
    ): Boolean {

        if (value == null) return true
        if (value is String) return value.isBlank()

        return false
    }
}
