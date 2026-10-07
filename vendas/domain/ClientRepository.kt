package com.samuelsott.vendas.domain

import com.samuelsott.vendas.data.dao.ClientDao
import com.samuelsott.vendas.data.model.Client
import kotlinx.coroutines.flow.Flow

class ClientRepository(private val clientDao: ClientDao) {
    val allClients: Flow<List<Client>> = clientDao.getAllClients()

    suspend fun insertClient(client: Client) {
        clientDao.insert(client)
    }
    suspend fun updateClient(client: Client) = clientDao.update(client)
    suspend fun getClientById(id: Int): Client? = clientDao.getClientById(id)
}