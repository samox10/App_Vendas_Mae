package com.samuelsott.vendas.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.samuelsott.vendas.VendasApplication
import com.samuelsott.vendas.data.model.Neighborhood
import com.samuelsott.vendas.domain.NeighborhoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NeighborhoodViewModel(private val repository: NeighborhoodRepository) : ViewModel() {

    // Guarda o nome do bairro que você está digitando
    private val _neighborhoodName = MutableStateFlow("")
    val neighborhoodName: StateFlow<String> = _neighborhoodName.asStateFlow()

    fun updateName(newName: String) {
        _neighborhoodName.value = newName
    }

    // Salva no banco de dados e avisa a tela para voltar
    fun saveNeighborhood(onSaveSuccess: () -> Unit) {
        val name = _neighborhoodName.value
        if (name.isBlank()) return // Não salva se estiver vazio

        viewModelScope.launch {
            repository.insertNeighborhood(Neighborhood(name = name))
            _neighborhoodName.value = "" // Limpa o campo
            onSaveSuccess()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as VendasApplication)
                NeighborhoodViewModel(application.container.neighborhoodRepository)
            }
        }
    }
}