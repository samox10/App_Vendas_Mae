package com.samuelsott.vendas.ui.admin

import android.net.Uri
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClientViewModel(
    private val clientRepository: ClientRepository,
    neighborhoodRepository: NeighborhoodRepository
) : ViewModel() {

    val neighborhoodsList: StateFlow<List<Neighborhood>> = neighborhoodRepository.allNeighborhoods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone = _phone.asStateFlow()

    private val _observation = MutableStateFlow("")
    val observation = _observation.asStateFlow()

    private val _selectedNeighborhoodId = MutableStateFlow<Int?>(null)
    val selectedNeighborhoodId = _selectedNeighborhoodId.asStateFlow()

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri = _photoUri.asStateFlow()

    // --- NOVAS VARIÁVEIS DO GPS ---
    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude = _longitude.asStateFlow()

    fun updateName(newName: String) { _name.value = newName }
    fun updatePhone(newPhone: String) { _phone.value = newPhone }
    fun updateObservation(newObs: String) { _observation.value = newObs }
    fun updateNeighborhood(id: Int) { _selectedNeighborhoodId.value = id }
    fun updatePhotoUri(uri: Uri?) { _photoUri.value = uri }

    // --- NOVA FUNÇÃO PARA SALVAR AS COORDENADAS ---
    fun updateLocation(lat: Double, lng: Double) {
        _latitude.value = lat
        _longitude.value = lng
    }

    // Adicione lá em cima, junto com as outras variáveis:
    private var _currentClientId: Int? = null

    // Adicione esta nova função para carregar o cliente quando for edição:
    fun loadClientForEdit(clientId: Int) {
        viewModelScope.launch {
            val client = clientRepository.getClientById(clientId)
            client?.let {
                _currentClientId = it.id
                _name.value = it.name
                _observation.value = it.observation
                _phone.value = it.phone
                _selectedNeighborhoodId.value = it.neighborhoodId
                updatePhotoUri(if (it.photoUri != null) android.net.Uri.parse(it.photoUri) else null)
                _latitude.value = it.latitude
                _longitude.value = it.longitude
            }
        }
    }

    // ATUALIZE a sua função saveClient para ficar assim:
    fun saveClient(onSaveSuccess: () -> Unit) {
        val neighborhoodId = _selectedNeighborhoodId.value
        if (_name.value.isNotBlank() && neighborhoodId != null) {
            viewModelScope.launch {
                val client = Client(
                    id = _currentClientId ?: 0, // Se tiver ID, ele sabe que é edição
                    name = _name.value,
                    observation = _observation.value,
                    phone = _phone.value,
                    neighborhoodId = neighborhoodId,
                    photoUri = _photoUri.value?.toString(),
                    latitude = _latitude.value,
                    longitude = _longitude.value
                )

                // Mágica: Decide automaticamente se salva novo ou atualiza
                if (_currentClientId == null) {
                    clientRepository.insertClient(client)
                } else {
                    clientRepository.updateClient(client)
                }
                onSaveSuccess()
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as VendasApplication)
                ClientViewModel(
                    application.container.clientRepository,
                    application.container.neighborhoodRepository
                )
            }
        }
    }
}