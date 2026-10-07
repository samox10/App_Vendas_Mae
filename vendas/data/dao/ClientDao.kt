package com.samuelsott.vendas.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.samuelsott.vendas.data.model.Client
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Insert
    suspend fun insert(client: Client)

    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<Client>>

    @androidx.room.Update
    suspend fun update(client: Client)

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getClientById(id: Int): Client?
}