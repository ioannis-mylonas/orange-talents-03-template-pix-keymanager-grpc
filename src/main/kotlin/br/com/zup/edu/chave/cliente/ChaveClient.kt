package br.com.zup.edu.chave.cliente

import br.com.zup.edu.TipoConta
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${pix.erp-url}")
interface ChaveClient {
    @Get("/api/v1/clientes/{id}/contas")
    fun buscaDetalhes(@PathVariable id: String, @QueryValue tipo: TipoConta): ClienteDetalhes
}