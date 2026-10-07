package com.samuelsott.vendas.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeighborhoodScreen(
    onNavigateBack: () -> Unit,
    viewModel: NeighborhoodViewModel = viewModel(factory = NeighborhoodViewModel.Factory)
) {
    val name by viewModel.neighborhoodName.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Adicionar Novo Bairro") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Nome do Bairro *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = { viewModel.saveNeighborhood(onNavigateBack) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() // O botão só liga se tiver texto
            ) {
                Text("Salvar Bairro")
            }
        }
    }
}