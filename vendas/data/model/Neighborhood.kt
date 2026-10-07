package com.samuelsott.vendas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "neighborhoods")
data class Neighborhood(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)