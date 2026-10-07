package com.samuelsott.vendas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val clientId: Int, // Vínculo com o Cliente
    val amount: Double,
    val paymentMethod: String,
    val status: String,
    val observations: String,
    val timestamp: Long
)