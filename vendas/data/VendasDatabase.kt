package com.samuelsott.vendas.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.samuelsott.vendas.data.dao.ClientDao
import com.samuelsott.vendas.data.dao.NeighborhoodDao
import com.samuelsott.vendas.data.model.Client
import com.samuelsott.vendas.data.model.Neighborhood
import com.samuelsott.vendas.data.model.Sale
import com.samuelsott.vendas.data.dao.SaleDao


// Adicionamos as três entidades na lista do banco de dados
@Database(entities = [Sale::class, Client::class, Neighborhood::class], version = 2, exportSchema = false)
abstract class VendasDatabase : RoomDatabase() {

    // Declaramos os três DAOs
    abstract fun saleDao(): SaleDao
    abstract fun clientDao(): ClientDao
    abstract fun neighborhoodDao(): NeighborhoodDao
}