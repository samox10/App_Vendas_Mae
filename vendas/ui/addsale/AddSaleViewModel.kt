package com.samuelsott.vendas.ui.addsale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.samuelsott.vendas.VendasApplication
import com.samuelsott.vendas.data.model.Client
import com.samuelsott.vendas.data.model.Neighborhood
import com.samuelsott.vendas.domain.ClientRepository
import com.samuelsott.vendas.domain.NeighborhoodRepository
import com.samuelsott.vendas.domain.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.samuelsott.vendas.data.model.Sale

class AddSaleViewModel(
    private val saleRepository: SaleRepository,
    clientRepository: ClientRepository,
    neighborhoodRepository: NeighborhoodRepository // Adicionado para puxar os bairros
) : ViewModel() {

    // Puxa a lista de bairros automaticamente
    val neighborhoodsList: StateFlow<List<Neighborhood>> = neighborhoodRepository.allNeighborhoods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _selectedNeighborhoodId = MutableStateFlow<Int?>(null)
    val selectedNeighborhoodId = _selectedNeighborhoodId.asStateFlow()

    private val _selectedClientId = MutableStateFlow<Int?>(null)
    val selectedClientId = _selectedClientId.asStateFlow()

    // MÁGICA: A lista de clientes agora é filtrada pelo bairro selecionado
    val clientsList: StateFlow<List<Client>> = combine(
        clientRepository.allClients,
        _selectedNeighborhoodId
    ) { clients, neighborhoodId ->
        if (neighborhoodId == null) {
            emptyList() // Retorna vazio se não tiver bairro selecionado
        } else {
            clients.filter { it.neighborhoodId == neighborhoodId }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _amount = MutableStateFlow("")
    val amount = _amount.asStateFlow()

    private val _paymentMethod = MutableStateFlow("Dinheiro")
    val paymentMethod = _paymentMethod.asStateFlow()

    private val _status = MutableStateFlow("Recebido")
    val status = _status.asStateFlow()

    private val _observations = MutableStateFlow("")
    val observations = _observations.asStateFlow()

    // Quando troca o bairro, limpa o cliente selecionado anteriormente
    fun updateNeighborhood(id: Int) {
        _selectedNeighborhoodId.value = id
        _selectedClientId.value = null
    }

    fun updateClient(id: Int) { _selectedClientId.value = id }
    fun updateAmount(newAmount: String) { _amount.value = newAmount }
    fun updatePaymentMethod(method: String) { _paymentMethod.value = method }
    fun updateStatus(newStatus: String) { _status.value = newStatus }
    fun updateObservations(obs: String) { _observations.value = obs }

    fun saveSale(onSaveSuccess: () -> Unit) {
        val clientId = _selectedClientId.value
        val amountValue = _amount.value.replace(",", ".").toDoubleOrNull()

        if (clientId == null || amountValue == null) return

        viewModelScope.launch {
            val sale = Sale(
                clientId = clientId,
                amount = amountValue,
                paymentMethod = _paymentMethod.value,
                status = _status.value,
                observations = _observations.value,
                timestamp = System.currentTimeMillis()
            )
            saleRepository.insertSale(sale)
            onSaveSuccess()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as VendasApplication)
                AddSaleViewModel(
                    application.container.saleRepository,
                    application.container.clientRepository,
                    application.container.neighborhoodRepository // Injetado aqui
                )
            }
        }
    }
}