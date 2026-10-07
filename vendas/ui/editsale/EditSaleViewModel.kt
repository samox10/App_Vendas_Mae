package com.samuelsott.vendas.ui.editsale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.samuelsott.vendas.VendasApplication
import com.samuelsott.vendas.data.model.Client
import com.samuelsott.vendas.data.model.Sale
import com.samuelsott.vendas.domain.ClientRepository
import com.samuelsott.vendas.domain.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditSaleViewModel(
    private val saleRepository: SaleRepository,
    clientRepository: ClientRepository
) : ViewModel() {

    val clientsList: StateFlow<List<Client>> = clientRepository.allClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _selectedClientId = MutableStateFlow<Int?>(null)
    val selectedClientId = _selectedClientId.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount = _amount.asStateFlow()

    private val _paymentMethod = MutableStateFlow("Dinheiro")
    val paymentMethod = _paymentMethod.asStateFlow()

    private val _status = MutableStateFlow("Recebido")
    val status = _status.asStateFlow()

    private val _observations = MutableStateFlow("")
    val observations = _observations.asStateFlow()

    // Guardamos a venda original para preservar a data/hora e o ID na hora de salvar a edição
    private var currentSale: Sale? = null

    // Função que busca a venda no banco e preenche todos os campos da tela
    fun loadSale(saleId: Int) {
        viewModelScope.launch {
            val sale = saleRepository.getSaleById(saleId)
            sale?.let {
                currentSale = it
                _selectedClientId.value = it.clientId
                _amount.value = it.amount.toString()
                _paymentMethod.value = it.paymentMethod
                _status.value = it.status
                _observations.value = it.observations
            }
        }
    }

    fun updateClient(id: Int) { _selectedClientId.value = id }
    fun updateAmount(newAmount: String) { _amount.value = newAmount }
    fun updatePaymentMethod(method: String) { _paymentMethod.value = method }
    fun updateStatus(newStatus: String) { _status.value = newStatus }
    fun updateObservations(obs: String) { _observations.value = obs }

    // Salva por cima da venda antiga (Atualiza)
    fun saveSale(onSaveSuccess: () -> Unit) {
        val clientId = _selectedClientId.value
        val amountValue = _amount.value.replace(",", ".").toDoubleOrNull()
        val originalSale = currentSale

        if (clientId == null || amountValue == null || originalSale == null) return

        viewModelScope.launch {
            val updatedSale = originalSale.copy(
                clientId = clientId,
                amount = amountValue,
                paymentMethod = _paymentMethod.value,
                status = _status.value,
                observations = _observations.value
                // Note que NÃO mudamos o timestamp, então a venda continua no mesmo dia!
            )
            saleRepository.updateSale(updatedSale)
            onSaveSuccess()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as VendasApplication)
                EditSaleViewModel(
                    application.container.saleRepository,
                    application.container.clientRepository
                )
            }
        }
    }
}