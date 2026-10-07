package com.samuelsott.vendas.ui.admin

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.Refresh


// Truque para criar o arquivo da foto direto na memória do celular antes da câmera abrir
fun createImageUri(context: Context): Uri? {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "cliente_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }
    return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientScreen(
    onNavigateBack: () -> Unit,
    clientId: Int? = null,
    viewModel: ClientViewModel = viewModel(factory = ClientViewModel.Factory)
) {
    LaunchedEffect(clientId) {
        if (clientId != null && clientId != -1) {
            viewModel.loadClientForEdit(clientId)
        }
    }
    val name by viewModel.name.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val observation by viewModel.observation.collectAsState()
    val selectedNeighborhoodId by viewModel.selectedNeighborhoodId.collectAsState()
    val neighborhoodsList by viewModel.neighborhoodsList.collectAsState()
    val photoUri by viewModel.photoUri.collectAsState()

    val latitude by viewModel.latitude.collectAsState()
    val longitude by viewModel.longitude.collectAsState()

    var expandedNeighborhood by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // --- CONFIGURAÇÕES DA CÂMERA ---
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            viewModel.updatePhotoUri(tempPhotoUri) // Salva a foto que acabamos de tirar
        }
    }

    // --- CONFIGURAÇÕES DO GPS ---
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun fetchLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                viewModel.updateLocation(location.latitude, location.longitude)
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchLocation()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Registrar Novo Cliente") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- CÍRCULO DA FOTO DO CLIENTE ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5)) // Fundo cinza clarinho combinando com o Select
                    .clickable {
                        val uri = createImageUri(context)
                        tempPhotoUri = uri
                        if (uri != null) { cameraLauncher.launch(uri) }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Foto Cliente",
                        contentScale = ContentScale.Fit, // <--- MUDAMOS DE CROP PARA FIT AQUI TAMBÉM
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Tirar Foto", modifier = Modifier.size(40.dp), tint = Color.Gray)
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Nome do Cliente *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { viewModel.updatePhone(it) },
                label = { Text("Telefone / WhatsApp (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone) // Abre o teclado de números
            )

            OutlinedTextField(
                value = observation,
                onValueChange = { viewModel.updateObservation(it) },
                label = { Text("Observação / Endereço / Ponto de Referência") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 4
            )

            val selectedNeighborhood = neighborhoodsList.find { it.id == selectedNeighborhoodId }
            ExposedDropdownMenuBox(
                expanded = expandedNeighborhood,
                onExpandedChange = { expandedNeighborhood = !expandedNeighborhood }
            ) {
                OutlinedTextField(
                    value = selectedNeighborhood?.name ?: "Selecione um Bairro *",
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

            Spacer(modifier = Modifier.height(8.dp))

            val onClickLocation = {
                val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                val hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                if (hasFineLocation || hasCoarseLocation) {
                    fetchLocation()
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                }
            }

            if (latitude != null && longitude != null) {
                // UX Melhorada: Cartão de sucesso com botão de ação separado
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "OK", tint = Color(0xFF388E3C))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GPS Salvo!", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold)
                        }

                        // Botão claro e específico para atualizar
                        TextButton(onClick = onClickLocation) {
                            Icon(Icons.Default.Refresh, contentDescription = "Atualizar", tint = Color(0xFF388E3C))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Atualizar", color = Color(0xFF388E3C))
                        }
                    }
                }
            } else {
                // Se não tem GPS, botão de ação principal
                Button(
                    onClick = onClickLocation,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "GPS")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salvar Minha Localização Atual")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.saveClient(onNavigateBack) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && selectedNeighborhoodId != null
            ) {
                Text("Salvar Cliente")
            }
        }
    }
}