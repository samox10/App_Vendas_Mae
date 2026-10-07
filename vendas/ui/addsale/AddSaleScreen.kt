package com.samuelsott.vendas.ui.addsale

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddSaleViewModel = viewModel(factory = AddSaleViewModel.Factory)
) {
    val neighborhoodsList by viewModel.neighborhoodsList.collectAsState()
    val selectedNeighborhoodId by viewModel.selectedNeighborhoodId.collectAsState()
    val clientsList by viewModel.clientsList.collectAsState()
    val selectedClientId by viewModel.selectedClientId.collectAsState()

    val amount by viewModel.amount.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val status by viewModel.status.collectAsState()
    val observations by viewModel.observations.collectAsState()

    var expandedNeighborhood by remember { mutableStateOf(false) }
    var expandedClient by remember { mutableStateOf(false) }
    var showMaximizedPhoto by remember { mutableStateOf(false) }

    val selectedClient = clientsList.find { it.id == selectedClientId }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Registrar Nova Venda") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. SELECT DE BAIRROS (Novo)
            ExposedDropdownMenuBox(
                expanded = expandedNeighborhood,
                onExpandedChange = { expandedNeighborhood = !expandedNeighborhood }
            ) {
                val selectedNeighborhoodName = neighborhoodsList.find { it.id == selectedNeighborhoodId }?.name ?: "1º Selecione o Bairro *"

                OutlinedTextField(
                    value = selectedNeighborhoodName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Bairro *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNeighborhood) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedNeighborhood,
                    onDismissRequest = { expandedNeighborhood = false }
                ) {
                    if (neighborhoodsList.isEmpty()) {
                        DropdownMenuItem(text = { Text("Nenhum bairro cadastrado") }, onClick = { expandedNeighborhood = false })
                    } else {
                        neighborhoodsList.forEach { neighborhood ->
                            DropdownMenuItem(
                                text = { Text(neighborhood.name) },
                                onClick = {
                                    viewModel.updateNeighborhood(neighborhood.id)
                                    expandedNeighborhood = false
                                }
                            )
                        }
                    }
                }
            }

            // 2. SELECT DE CLIENTES (Atualizado com Foto)
            ExposedDropdownMenuBox(
                expanded = expandedClient,
                onExpandedChange = {
                    // Só deixa abrir se tiver um bairro selecionado
                    if (selectedNeighborhoodId != null) {
                        expandedClient = !expandedClient
                    }
                }
            ) {
                val selectedClientName = selectedClient?.name ?: if (selectedNeighborhoodId == null) "Aguardando Bairro..." else "2º Selecione o Cliente *"

                OutlinedTextField(
                    value = selectedClientName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cliente *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClient) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    enabled = selectedNeighborhoodId != null // Desabilita visualmente se não tiver bairro
                )

                ExposedDropdownMenu(
                    expanded = expandedClient,
                    onDismissRequest = { expandedClient = false }
                ) {
                    if (clientsList.isEmpty()) {
                        DropdownMenuItem(text = { Text("Nenhum cliente neste bairro") }, onClick = { expandedClient = false })
                    } else {
                        clientsList.forEach { client ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Mostra a foto redondinha do cliente dentro da lista
                                        if (client.photoUri != null) {
                                            AsyncImage(
                                                model = client.photoUri,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.size(32.dp).clip(CircleShape)
                                            )
                                        } else {
                                            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(client.name)
                                    }
                                },
                                onClick = {
                                    viewModel.updateClient(client.id)
                                    expandedClient = false
                                }
                            )
                        }
                    }
                }
            }

            // 1.5 Miniatura da Foto do Cliente Selecionado
            if (selectedClient?.photoUri != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    AsyncImage(
                        model = selectedClient.photoUri,
                        contentDescription = "Miniatura",
                        contentScale = ContentScale.Fit, // <--- MUDAMOS DE CROP PARA FIT
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp) // Aumentamos um pouquinho a altura para a foto aparecer melhor
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5F5)) // Fundo cinza bem clarinho para preencher as bordas
                            .clickable { showMaximizedPhoto = true }
                    )
                }
            }

            // 3. Campo de Valor
            OutlinedTextField(
                value = amount,
                onValueChange = { viewModel.updateAmount(it) },
                label = { Text("Valor da Venda (R$) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 4. Métodos e Status
            Text("Método de Pagamento", style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Dinheiro", "Cartão", "Pix").forEach { method ->
                    FilterChip(selected = paymentMethod == method, onClick = { viewModel.updatePaymentMethod(method) }, label = { Text(method) })
                }
            }

            Text("Status", style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Recebido", "Pendente").forEach { s ->
                    FilterChip(selected = status == s, onClick = { viewModel.updateStatus(s) }, label = { Text(s) })
                }
            }

            // 5. Observações
            OutlinedTextField(
                value = observations,
                onValueChange = { viewModel.updateObservations(it) },
                label = { Text("Observações (Opcional)") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.saveSale(onNavigateBack) },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedClientId != null && amount.isNotBlank()
            ) {
                Text("Salvar Venda")
            }
        }

        // --- JANELA FLUTUANTE DA FOTO AMPLIADA ---
        if (showMaximizedPhoto && selectedClient?.photoUri != null) {
            Dialog(onDismissRequest = { showMaximizedPhoto = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                Box(modifier = Modifier.fillMaxSize().clickable { showMaximizedPhoto = false }, contentAlignment = Alignment.Center) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.8f)) {}
                    AsyncImage(
                        model = selectedClient.photoUri,
                        contentDescription = "Foto Ampliada",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth(0.9f).clip(RoundedCornerShape(16.dp))
                    )
                }
            }
        }
    }
}