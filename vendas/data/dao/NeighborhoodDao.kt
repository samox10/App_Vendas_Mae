package com.samuelsott.vendas.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.samuelsott.vendas.data.model.Neighborhood
import kotlinx.coroutines.flow.Flow

@Dao
interface NeighborhoodDao {
    @Insert
    suspend fun insert(neighborhood: Neighborhood)

    // Busca todos os bairros e já os entrega em ordem alfabética
    @Query("SELECT * FROM neighborhoods ORDER BY name ASC")
    fun getAllNeighborhoods(): Flow<List<Neighborhood>>
}