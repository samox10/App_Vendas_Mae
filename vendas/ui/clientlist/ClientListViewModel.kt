package com.samuelsott.vendas.ui.clientlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.samuelsott.vendas.VendasApplication
import com.samuelsott.vendas.domain.ClientRepository
import com.samuelsott.vendas.domain.NeighborhoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class ClientListViewModel(
    clientRepository: ClientRepository,
    neighborhoodRepository: NeighborhoodRepository
) : ViewModel() {

    val allClients = clientRepository.allClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allNeighborhoods = neighborhoodRepository.allNeighborhoods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Guarda qual bairro está selecionado no filtro (nulo = mostra todos)
    private val _selectedNeighborhoodId = MutableStateFlow<Int?>(null)
    val selectedNeighborhoodId = _selectedNeighborhoodId.asStateFlow()

    fun updateFilter(neighborhoodId: Int?) {
        _selectedNeighborhoodId.value = neighborhoodId
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as VendasApplication)
                ClientListViewModel(
                    application.container.clientRepository,
                    application.container.neighborhoodRepository
                )
            }
        }
    }
}