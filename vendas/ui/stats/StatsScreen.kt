package com.samuelsott.vendas.ui.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Resumo", "Bairros", "Clientes")
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estatísticas Inteligentes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> ResumoTab(uiState, formatter)
                    1 -> BairrosTab(uiState.neighborhoodStats, formatter)
                    2 -> ClientesTab(uiState.allClientStats, formatter)
                }
            }
        }
    }
}

// ==========================================
// ABA 1: RESUMO GERAL
// ==========================================
@Composable
fun ResumoTab(state: StatsUiState, formatter: NumberFormat) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balanço: Pendente vs Recebido
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)) // Vermelho claro
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Na Rua (Pendente)", color = Color(0xFFD32F2F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(formatter.format(state.pending), color = Color(0xFFD32F2F), fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)) // Verde claro
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Em Caixa (Recebido)", color = Color(0xFF388E3C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(formatter.format(state.received), color = Color(0xFF388E3C), fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Insights de Negócio
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ticket Médio", color = Color.Gray, fontSize = 12.sp)
                    Text(formatter.format(state.ticketMedio), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Melhor Bairro", color = Color.Gray, fontSize = 12.sp)
                    Text(state.bestNeighborhood, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Top 3 Clientes Ouro", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)

        state.topClients.forEachIndexed { index, client ->
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ícone de Troféu com cores (Ouro, Prata, Bronze)
                    val tint = when(index) {
                        0 -> Color(0xFFFFD700)
                        1 -> Color(0xFFC0C0C0)
                        else -> Color(0xFFCD7F32)
                    }
                    Icon(Icons.Default.EmojiEvents, contentDescription = "Troféu", tint = tint, modifier = Modifier.size(32.dp))

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(client.clientName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${client.purchaseCount} compras registradas", fontSize = 12.sp, color = Color.Gray)
                    }

                    Text(formatter.format(client.totalSpent), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// ==========================================
// ABA 2: DESEMPENHO POR BAIRRO (Com Sanfona)
// ==========================================
@Composable
fun BairrosTab(neighborhoods: List<NeighborhoodStat>, formatter: NumberFormat) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(neighborhoods) { nStat ->
            // Controla se a lista de clientes deste bairro está aberta ou fechada
            var expanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Cabeçalho do Bairro
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(nStat.neighborhoodName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${nStat.clientCount} clientes cadastrados", fontSize = 12.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatter.format(nStat.totalSpent), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = "Expandir")
                        }
                    }

                    // A Sanfona que abre os Clientes
                    AnimatedVisibility(visible = expanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp)
                        ) {
                            if (nStat.clients.isEmpty()) {
                                Text("Nenhuma venda registrada neste bairro.", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                Text("Ranking de Clientes:", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                                nStat.clients.forEachIndexed { index, client ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${index + 1}º ${client.clientName}", fontSize = 14.sp)
                                        Text(formatter.format(client.totalSpent), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// ABA 3: RANKING GERAL DE CLIENTES
// ==========================================
@Composable
fun ClientesTab(clients: List<ClientStat>, formatter: NumberFormat) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(clients) { index, client ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(client.clientName, fontWeight = FontWeight.Bold)
                        Text("${client.purchaseCount} compras", fontSize = 12.sp, color = Color.Gray)
                    }

                    Text(formatter.format(client.totalSpent), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}