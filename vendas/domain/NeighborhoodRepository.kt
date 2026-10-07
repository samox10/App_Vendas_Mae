package com.samuelsott.vendas.domain

import com.samuelsott.vendas.data.dao.NeighborhoodDao
import com.samuelsott.vendas.data.model.Neighborhood
import kotlinx.coroutines.flow.Flow

class NeighborhoodRepository(private val neighborhoodDao: NeighborhoodDao) {
    val allNeighborhoods: Flow<List<Neighborhood>> = neighborhoodDao.getAllNeighborhoods()

    suspend fun insertNeighborhood(neighborhood: Neighborhood) {
        neighborhoodDao.insert(neighborhood)
    }
}