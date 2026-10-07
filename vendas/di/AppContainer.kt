package com.samuelsott.vendas.di

import android.content.Context
import androidx.room.Room
import com.samuelsott.vendas.data.VendasDatabase
import com.samuelsott.vendas.domain.ClientRepository
import com.samuelsott.vendas.domain.NeighborhoodRepository
import com.samuelsott.vendas.domain.SaleRepository

interface AppContainer {
    val saleRepository: SaleRepository
    val clientRepository: ClientRepository
    val neighborhoodRepository: NeighborhoodRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: VendasDatabase by lazy {
        Room.databaseBuilder(
            context,
            VendasDatabase::class.java,
            "vendas_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    override val saleRepository: SaleRepository by lazy {
        SaleRepository(database.saleDao())
    }

    override val clientRepository: ClientRepository by lazy {
        ClientRepository(database.clientDao())
    }

    override val neighborhoodRepository: NeighborhoodRepository by lazy {
        NeighborhoodRepository(database.neighborhoodDao())
    }
}