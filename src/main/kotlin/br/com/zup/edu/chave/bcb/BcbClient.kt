package br.com.zup.edu.chave.bcb

import br.com.zup.edu.Open
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import java.net.http.HttpResponse
import javax.validation.Valid

@Client("\${pix.bcb-url}")
interface BcbClient {
    @Post(value = "/api/v1/pix/keys",consumes = ["application/xml"], produces = ["application/xml"])
    fun cadastra(@Body @Valid createPixKeyRequest: BcbCreatePixKeyRequest): BcbCreatePixKeyResponse?

    @Delete(value = "/api/v1/pix/keys/{key}", consumes = ["application/xml"], produces = ["application/xml"])
    fun deleta(@QueryValue key: String, @Body @Valid deletePixKeyRequest: BcbDeletePixKeyRequest)
}