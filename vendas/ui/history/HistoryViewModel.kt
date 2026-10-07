package com.samuelsott.vendas.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.samuelsott.vendas.VendasApplication
import com.samuelsott.vendas.data.model.SaleWithClient
import com.samuelsott.vendas.domain.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.samuelsott.vendas.data.model.Neighborhood
import com.samuelsott.vendas.domain.NeighborhoodRepository

class HistoryViewModel(
    private val saleRepository: SaleRepository,
    neighborhoodRepository: NeighborhoodRepository // 1. ADICIONADO AQUI
) : ViewModel() {
    val neighborhoodsList: StateFlow<List<Neighborhood>> = neighborhoodRepository.allNeighborhoods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val allSales = saleRepository.allSalesWithClients

    private val currentCalendar = Calendar.getInstance()
    private val _selectedMonth = MutableStateFlow(currentCalendar.get(Calendar.MONTH))
    private val _selectedYear = MutableStateFlow(currentCalendar.get(Calendar.YEAR))

    // NOVO: Controle do filtro selecionado
    private val _currentFilter = MutableStateFlow("Todos") // Opções: "Todos", "Recebido", "Pendente"

    val selectedMonth = _selectedMonth.asStateFlow()
    val selectedYear = _selectedYear.asStateFlow()
    val currentFilter = _currentFilter.asStateFlow()

    fun nextMonth() {
        if (_selectedMonth.value == 11) {
            _selectedMonth.value = 0
            _selectedYear.value += 1
        } else {
            _selectedMonth.value += 1
        }
    }

    fun previousMonth() {
        if (_selectedMonth.value == 0) {
            _selectedMonth.value = 11
            _selectedYear.value -= 1
        } else {
            _selectedMonth.value -= 1
        }
    }

    // NOVO: Função para mudar o filtro
    fun updateFilter(filter: String) {
        _currentFilter.value = filter
    }

    // MATEMÁTICA ATUALIZADA: Agora considera o filtro para somar a Renda do Mês
    val monthlyIncome: StateFlow<Double> = combine(allSales, _selectedMonth, _selectedYear, _currentFilter) { sales, month, year, filter ->
        sales.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.sale.timestamp }
            cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year &&
                    (filter == "Todos" || it.sale.status == filter) // Aplica o filtro aqui!
        }.sumOf { it.sale.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0.0)

    // MATEMÁTICA ATUALIZADA: Considera o filtro para a Renda do Ano
    val yearlyIncome: StateFlow<Double> = combine(allSales, _selectedYear, _currentFilter) { sales, year, filter ->
        sales.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.sale.timestamp }
            cal.get(Calendar.YEAR) == year &&
                    (filter == "Todos" || it.sale.status == filter)
        }.sumOf { it.sale.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0.0)

    // AGRUPAMENTO ATUALIZADO: Filtra as vendas antes de agrupar pelos dias
    val salesGroupedByDay: StateFlow<Map<String, List<SaleWithClient>>> = combine(allSales, _selectedMonth, _selectedYear, _currentFilter) { sales, month, year, filter ->
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val filteredSales = sales.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.sale.timestamp }
            cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year &&
                    (filter == "Todos" || it.sale.status == filter)
        }

        filteredSales.groupBy { dateFormat.format(Date(it.sale.timestamp)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as VendasApplication)
                HistoryViewModel(
                    application.container.saleRepository,
                    application.container.neighborhoodRepository // 3. ADICIONADO AQUI
                )
            }
        }
    }
}