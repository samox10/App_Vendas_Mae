package com.samuelsott.vendas.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.samuelsott.vendas.VendasApplication
import com.samuelsott.vendas.domain.ClientRepository
import com.samuelsott.vendas.domain.NeighborhoodRepository
import com.samuelsott.vendas.domain.SaleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// --- CLASSES DE AJUDA PARA ORGANIZAR OS DADOS ---
data class ClientStat(
    val clientId: Int,
    val clientName: String,
    val neighborhoodId: Int,
    val totalSpent: Double,
    val purchaseCount: Int
)

data class NeighborhoodStat(
    val neighborhoodId: Int,
    val neighborhoodName: String,
    val totalSpent: Double,
    val clientCount: Int,
    val clients: List<ClientStat>
)

data class StatsUiState(
    val received: Double = 0.0,
    val pending: Double = 0.0,
    val ticketMedio: Double = 0.0,
    val bestNeighborhood: String = "Nenhum",
    val topClients: List<ClientStat> = emptyList(),
    val allClientStats: List<ClientStat> = emptyList(),
    val neighborhoodStats: List<NeighborhoodStat> = emptyList()
)

class StatsViewModel(
    saleRepository: SaleRepository,
    neighborhoodRepository: NeighborhoodRepository,
    clientRepository: ClientRepository
) : ViewModel() {

    // A Mágica: Junta Vendas, Bairros e Clientes em um lugar só
    val uiState = combine(
        saleRepository.allSalesWithClients,
        neighborhoodRepository.allNeighborhoods,
        clientRepository.allClients
    ) { salesWithClients, neighborhoods, allClients ->

        // 1. Cálculos Gerais (Caixa e Ticket Médio)
        val received = salesWithClients.filter { it.sale.status == "Recebido" }.sumOf { it.sale.amount }
        val pending = salesWithClients.filter { it.sale.status == "Pendente" }.sumOf { it.sale.amount }
        val totalRevenue = received + pending
        val ticketMedio = if (salesWithClients.isNotEmpty()) totalRevenue / salesWithClients.size else 0.0

        // 2. Estatísticas por Cliente (Agrupando histórico)
        val clientGroups = salesWithClients.groupBy { it.client.id }
        val clientStats = allClients.map { client ->
            val sales = clientGroups[client.id] ?: emptyList()
            ClientStat(
                clientId = client.id,
                clientName = client.name,
                neighborhoodId = client.neighborhoodId,
                totalSpent = sales.sumOf { it.sale.amount },
                purchaseCount = sales.size
            )
        }.sortedByDescending { it.totalSpent }

        // 3. Estatísticas por Bairro (Com lista de clientes ouro de cada um)
        val neighborhoodStats = neighborhoods.map { neighborhood ->
            val clientsInNeighborhood = clientStats.filter { it.neighborhoodId == neighborhood.id }
            NeighborhoodStat(
                neighborhoodId = neighborhood.id,
                neighborhoodName = neighborhood.name,
                totalSpent = clientsInNeighborhood.sumOf { it.totalSpent },
                clientCount = clientsInNeighborhood.size,
                // Clientes do bairro, do maior pro menor, apenas os que já compraram algo
                clients = clientsInNeighborhood.filter { it.purchaseCount > 0 }.sortedByDescending { it.totalSpent }
            )
        }.sortedByDescending { it.totalSpent }

        // Monta o Pacote final que vai para a Tela
        StatsUiState(
            received = received,
            pending = pending,
            ticketMedio = ticketMedio,
            bestNeighborhood = neighborhoodStats.firstOrNull { it.totalSpent > 0 }?.neighborhoodName ?: "Nenhum",
            topClients = clientStats.filter { it.purchaseCount > 0 }.take(3), // Pódio dos 3 melhores
            allClientStats = clientStats.filter { it.purchaseCount > 0 },
            neighborhoodStats = neighborhoodStats
        )

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), StatsUiState())

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as VendasApplication)
                StatsViewModel(
                    application.container.saleRepository,
                    application.container.neighborhoodRepository,
                    application.container.clientRepository
                )
            }
        }
    }
}