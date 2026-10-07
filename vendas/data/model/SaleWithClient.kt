package com.samuelsott.vendas.data.model

import androidx.room.Embedded
import androidx.room.Relation

// Esta classe não é uma tabela nova, é apenas uma forma de "juntar" os dados para a tela ler
data class SaleWithClient(
    @Embedded val sale: Sale,

    @Relation(
        parentColumn = "clientId",
        entityColumn = "id"
    )
    val client: Client
)