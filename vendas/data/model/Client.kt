package com.samuelsott.vendas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val observation: String = "", // Devolvendo o seu campo original
    val phone: String = "",       // Deixando opcional para não dar erro
    val neighborhoodId: Int,
    val photoUri: String? = null,

    // NOVOS CAMPOS PARA O GPS OFFLINE
    val latitude: Double? = null,
    val longitude: Double? = null
)