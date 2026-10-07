package com.samuelsott.vendas.ui.clientlist

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Phone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditClient: (Int) -> Unit, // <--- NOVO
    viewModel: ClientListViewModel = viewModel(factory = ClientListViewModel.Factory)
) {
    val clients by viewModel.allClients.collectAsState()
    val neighborhoods by viewModel.allNeighborhoods.collectAsState()
    val selectedNeighborhoodId by viewModel.selectedNeighborhoodId.collectAsState()

    var expandedMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Aplica o filtro de bairro
    val filteredClients = if (selectedNeighborhoodId == null) {
        clients
    } else {
        clients.filter { it.neighborhoodId == selectedNeighborhoodId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Clientes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {

            // --- FILTRO DE BAIRRO NO TOPO ---
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)) {
                val selectedName = neighborhoods.find { it.id == selectedNeighborhoodId }?.name ?: "Todos os Bairros"

                ExposedDropdownMenuBox(
                    expanded = expandedMenu,
                    onExpandedChange = { expandedMenu = !expandedMenu }
                ) {
                    OutlinedTextField(
                        value = selectedName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Filtrar por Bairro") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMenu) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        // Opção para ver todos
                        DropdownMenuItem(
                            text = { Text("Todos os Bairros") },
                            onClick = { viewModel.updateFilter(null); expandedMenu = false }
                        )
                        HorizontalDivider()
                        // Lista de bairros
                        neighborhoods.forEach { neighborhood ->
                            DropdownMenuItem(
                                text = { Text(neighborhood.name) },
                                onClick = { viewModel.updateFilter(neighborhood.id); expandedMenu = false }
                            )
                        }
                    }
                }
            }

            // --- LISTA DE PERFIS DOS CLIENTES ---
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredClients) { client ->
                    val neighborhoodName = neighborhoods.find { it.id == client.neighborhoodId }?.name ?: "Bairro Desconhecido"

                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Foto ou Ícone Padrão
                                if (client.photoUri != null) {
                                    AsyncImage(
                                        model = client.photoUri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        // Mais larga do que alta, com borda suave
                                        modifier = Modifier.size(width = 80.dp, height = 60.dp).clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Box(modifier = Modifier.size(width = 80.dp, height = 60.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(client.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text(neighborhoodName, color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                            if (client.phone.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = "Telefone", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(client.phone, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            if (client.observation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Observações/Endereço:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(client.observation, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { onNavigateToEditClient(client.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Editar Cadastro")
                            }

                            // BOTÃO DO GOOGLE MAPS SE TIVER GPS SALVO
                            if (client.latitude != null && client.longitude != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        // MÁGICA: Abre o Google Maps Offline direto na coordenada
                                        val uri = Uri.parse("google.navigation:q=${client.latitude},${client.longitude}")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        try {
                                            context.startActivity(mapIntent)
                                        } catch (e: ActivityNotFoundException) {
                                            // Se não tiver o Maps instalado, abre no navegador
                                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${client.latitude},${client.longitude}"))
                                            context.startActivity(browserIntent)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)) // Verde Waze/Maps
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = "Mapa")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Traçar Rota no Mapa")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}