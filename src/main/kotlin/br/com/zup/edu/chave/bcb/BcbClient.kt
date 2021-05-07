package br.com.zup.edu.chave.bcb

import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import javax.validation.Valid

@Client("\${pix.bcb-url}")
interface BcbClient {
    @Post(value = "/api/v1/pix/keys",consumes = ["application/xml"], produces = ["application/xml"])
    fun cadastra(@Body @Valid createPixKeyRequest: BcbCreatePixKeyRequest): BcbCreatePixKeyResponse

    @Delete(value = "/api/v1/pix/keys/{key}", consumes = ["application/xml"], produces = ["application/xml"])
    fun deleta(@QueryValue key: String, @Body @Valid deletePixKeyRequest: BcbDeletePixKeyRequest)

    @Get(value = "/api/v1/pix/keys", consumes = ["application/xml"], produces = ["application/xml"])
    fun lista(): BcbKeyDetailsList

    @Get(value = "/api/v1/pix/keys/{key}", consumes = ["application/xml"], produces = ["application/xml"])
    fun get(@QueryValue key: String): BcbKeyDetails?
}