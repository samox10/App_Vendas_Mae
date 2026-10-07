package com.samuelsott.vendas.domain

import com.samuelsott.vendas.data.dao.SaleDao
import com.samuelsott.vendas.data.model.Sale
import com.samuelsott.vendas.data.model.SaleWithClient
import kotlinx.coroutines.flow.Flow

class SaleRepository(private val saleDao: SaleDao) {

    // Agora o repositório entrega a Venda já com os dados do Cliente (Nome, Foto, etc)
    val allSalesWithClients: Flow<List<SaleWithClient>> = saleDao.getAllSalesWithClients()

    suspend fun insertSale(sale: Sale) {
        saleDao.insertSale(sale)
    }
    suspend fun updateSale(sale: Sale) {
        saleDao.updateSale(sale)
    }

    suspend fun getSaleById(id: Int): Sale? {
        return saleDao.getSaleById(id)
    }
}