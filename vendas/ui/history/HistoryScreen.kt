package com.samuelsott.vendas.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.samuelsott.vendas.data.model.SaleWithClient
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateToAdmin: () -> Unit,
    onNavigateToEditSale: (Int) -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val monthlyIncome by viewModel.monthlyIncome.collectAsState()
    val yearlyIncome by viewModel.yearlyIncome.collectAsState()
    val salesGroupedByDay by viewModel.salesGroupedByDay.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val neighborhoodsList by viewModel.neighborhoodsList.collectAsState()

    var saleToShowDetails by remember { mutableStateOf<SaleWithClient?>(null) }

    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val monthNames = listOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard de Vendas") },
                actions = {
                    // Botão de Engrenagem para ir para o Menu de Administração
                    IconButton(onClick = onNavigateToAdmin) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Administração")
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
            // --- CABEÇALHO: CONTROLE DE MÊS E ANO ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Mês Anterior")
                }
                Text(
                    text = "${monthNames[selectedMonth]} de $selectedYear",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Próximo Mês")
                }
            }

            // --- CARDS DE RENDA ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card Mês
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Renda do Mês", style = MaterialTheme.typography.labelMedium)
                        Text(formatter.format(monthlyIncome), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }

                // Card Ano
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Renda do Ano", style = MaterialTheme.typography.labelMedium)
                        Text(formatter.format(yearlyIncome), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todos", "Recebido", "Pendente").forEach { filter ->
                    FilterChip(
                        selected = currentFilter == filter,
                        onClick = { viewModel.updateFilter(filter) },
                        label = { Text(filter) }
                    )
                }
            }

            // --- LISTA DE VENDAS AGRUPADAS POR DIA ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (salesGroupedByDay.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhuma venda registrada neste mês.",
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            color = Color.Gray
                        )
                    }
                }

                salesGroupedByDay.forEach { (dateString, salesList) ->
                    item {
                        // Calcula o total apenas das vendas deste dia específico
                        val dailyTotal = salesList.sumOf { it.sale.amount }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dia $dateString",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = formatter.format(dailyTotal),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    // Vendas daquele dia
                    items(salesList) { saleWithClient ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { saleToShowDetails = saleWithClient },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Foto em miniatura
                                if (saleWithClient.client.photoUri != null) {
                                    AsyncImage(
                                        model = saleWithClient.client.photoUri,
                                        contentDescription = "Foto Cliente",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(48.dp).clip(CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray),
                                        contentAlignment = Alignment.Center
                                    ) { Text(saleWithClient.client.name.take(1)) }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Infos Básicas
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(saleWithClient.client.name, fontWeight = FontWeight.Bold)

                                    // NOVO: Busca o nome do bairro do cliente e exibe
                                    val clientNeighborhood = neighborhoodsList.find { it.id == saleWithClient.client.neighborhoodId }?.name ?: "Bairro desconhecido"
                                    Text(clientNeighborhood, fontSize = 12.sp, color = Color.DarkGray)

                                    Text("${saleWithClient.sale.paymentMethod} • ${saleWithClient.sale.status}", fontSize = 12.sp, color = Color.Gray)
                                }

                                // Valor e Cor do Status
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(formatter.format(saleWithClient.sale.amount), fontWeight = FontWeight.Bold)
                                    val statusColor = if (saleWithClient.sale.status == "Recebido") Color(0xFF4CAF50) else Color(0xFFF44336)
                                    Text(saleWithClient.sale.status, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- JANELA DE DETALHES (DIALOG) ---
        saleToShowDetails?.let { details ->
            Dialog(onDismissRequest = { saleToShowDetails = null }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (details.client.photoUri != null) {
                            AsyncImage(
                                model = details.client.photoUri,
                                contentDescription = "Foto",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(100.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Text(details.client.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(formatter.format(details.sale.amount), fontSize = 24.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp))

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
                        val dateFormatted = dateFormat.format(Date(details.sale.timestamp))
                        val dialogNeighborhood = neighborhoodsList.find { it.id == details.client.neighborhoodId }?.name ?: "Não informado"

                        DetailRow("Data e Hora:", dateFormatted)
                        DetailRow("Bairro:", dialogNeighborhood)
                        DetailRow("Método:", details.sale.paymentMethod)
                        DetailRow("Status:", details.sale.status)

                        if (details.sale.observations.isNotBlank()) {
                            DetailRow("Observação:", details.sale.observations)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // --- NOVO BOTÃO DE ROTA NO DASHBOARD ---
                        val context = LocalContext.current
                        if (details.client.latitude != null && details.client.longitude != null) {
                            Button(
                                onClick = {
                                    val uri = Uri.parse("google.navigation:q=${details.client.latitude},${details.client.longitude}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    try { context.startActivity(mapIntent) }
                                    catch (e: ActivityNotFoundException) { }
                                },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                            ) {
                                Icon(Icons.Default.Map, contentDescription = "Mapa")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Traçar Rota no Mapa")
                            }
                        }
                        // ---------------------------------------
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(onClick = { saleToShowDetails = null }) {
                                Text("Fechar")
                            }
                            Button(
                                onClick = {
                                    val saleId = details.sale.id
                                    saleToShowDetails = null // Fecha a janela
                                    onNavigateToEditSale(saleId) // Vai para a tela de edição
                                }
                            ) {
                                Text("Editar Venda")
                            }
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        Text(value, fontWeight = FontWeight.Medium)
    }
}