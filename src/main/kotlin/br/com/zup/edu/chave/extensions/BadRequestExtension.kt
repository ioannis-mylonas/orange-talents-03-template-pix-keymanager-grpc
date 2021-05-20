package br.com.zup.edu.chave.extensions

import com.google.rpc.BadRequest
import javax.validation.ConstraintViolation

/**
 * Converte uma lista de constraint violations em BadRequest
 * @return BadRequest com as constraint violations transformadas em FieldViolation
 */
fun Set<ConstraintViolation<*>>.toBadRequest(): BadRequest = BadRequest.newBuilder()
    .addAllFieldViolations(map { it.toFieldViolation() }).build()

/**
 * Converte uma lista de constraint violations em BadRequest e empacota
 * para transferência por gRPC
 * @return Any com os dados das constraint violations
 */
fun Set<ConstraintViolation<*>>.toPackedBadRequest(): com.google.protobuf.Any = com.google.protobuf.Any.pack(toBadRequest())

/**
 * Converte uma ConstraintViolation para um FieldViolation
 * @return FieldViolation com field e description
 */
fun ConstraintViolation<*>.toFieldViolation():
        BadRequest.FieldViolation = BadRequest.FieldViolation.newBuilder()
    .setField(propertyPath.last().name ?: "unknown field")
    .setDescription(message)
    .build()

/**
 * Converte um protobuf Any em um BadRequest
 * @return BadRequest com seus dados
 */
fun com.google.protobuf.Any.asBadRequest(): BadRequest = unpack(BadRequest::class.java)

/**
 * Verifica se todas as violações de constraint e mensagens estão presentes na BadRequest.
 * @param violations Violações formatadas como (campo, mensagem)
 * @return True se todas estiverem presentes, false caso contrário
 */
fun BadRequest.hasFieldViolations(violations: Set<Pair<String, String>>): Boolean {
    violations.forEach { violation ->
        fieldViolationsList.firstOrNull {
            it.field == violation.first && it.description == violation.second
        } ?: return false
    }

    return true
}