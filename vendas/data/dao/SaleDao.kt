package com.samuelsott.vendas.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.samuelsott.vendas.data.model.Sale
import com.samuelsott.vendas.data.model.SaleWithClient
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Insert
    suspend fun insertSale(sale: Sale)

    // O @Transaction garante que ele busque a venda e o cliente ao mesmo tempo sem falhas
    @Transaction
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSalesWithClients(): Flow<List<SaleWithClient>>
    @androidx.room.Update
    suspend fun updateSale(sale: Sale)

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Int): Sale?
}
