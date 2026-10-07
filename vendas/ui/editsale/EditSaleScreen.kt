package com.samuelsott.vendas.ui.editsale

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSaleScreen(
    saleId: Int, // Recebe qual venda precisamos editar
    onNavigateBack: () -> Unit,
    viewModel: EditSaleViewModel = viewModel(factory = EditSaleViewModel.Factory)
) {
    // Carrega os dados da venda no exato momento que a tela abre
    LaunchedEffect(saleId) {
        viewModel.loadSale(saleId)
    }

    val clientsList by viewModel.clientsList.collectAsState()
    val selectedClientId by viewModel.selectedClientId.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val status by viewModel.status.collectAsState()
    val observations by viewModel.observations.collectAsState()

    var expandedClient by remember { mutableStateOf(false) }
    var showMaximizedPhoto by remember { mutableStateOf(false) }
    val selectedClient = clientsList.find { it.id == selectedClientId }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editar Venda") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. Select de Clientes
            ExposedDropdownMenuBox(
                expanded = expandedClient,
                onExpandedChange = { expandedClient = !expandedClient }
            ) {
                val selectedClientName = selectedClient?.name ?: "Carregando..."

                OutlinedTextField(
                    value = selectedClientName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cliente *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClient) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedClient,
                    onDismissRequest = { expandedClient = false }
                ) {
                    clientsList.forEach { client ->
                        DropdownMenuItem(
                            text = { Text(client.name) },
                            onClick = {
                                viewModel.updateClient(client.id)
                                expandedClient = false
                            }
                        )
                    }
                }
            }

            // 1.5 Miniatura da Foto
            if (selectedClient?.photoUri != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    AsyncImage(
                        model = selectedClient.photoUri,
                        contentDescription = "Miniatura",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(80.dp).clip(CircleShape).clickable { showMaximizedPhoto = true }
                    )
                }
            }

            // 2. Campo de Valor
            OutlinedTextField(
                value = amount,
                onValueChange = { viewModel.updateAmount(it) },
                label = { Text("Valor da Venda (R$) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 3. Método de Pagamento
            Text("Método de Pagamento", style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Dinheiro", "Cartão", "Pix").forEach { method ->
                    FilterChip(
                        selected = paymentMethod == method,
                        onClick = { viewModel.updatePaymentMethod(method) },
                        label = { Text(method) }
                    )
                }
            }

            // 4. Status
            Text("Status", style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Recebido", "Pendente").forEach { s ->
                    FilterChip(
                        selected = status == s,
                        onClick = { viewModel.updateStatus(s) },
                        label = { Text(s) }
                    )
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
                Text("Salvar Alterações")
            }
        }

        // Janela flutuante da foto ampliada
        if (showMaximizedPhoto && selectedClient?.photoUri != null) {
            Dialog(
                onDismissRequest = { showMaximizedPhoto = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().clickable { showMaximizedPhoto = false },
                    contentAlignment = Alignment.Center
                ) {
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