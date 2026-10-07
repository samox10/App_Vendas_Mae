package com.samuelsott.vendas.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.samuelsott.vendas.utils.exportDatabaseBackup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Restore
import com.samuelsott.vendas.utils.restoreDatabaseBackup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuScreen(
    onNavigateToAddNeighborhood: () -> Unit,
    onNavigateToAddClient: () -> Unit,
    onNavigateToAddSale: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToClientList: () -> Unit //
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Menu Principal") }) }
    ) { innerPadding ->
        val context = LocalContext.current
        val restoreLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenMultipleDocuments()
        ) { uris ->
            if (uris.isNotEmpty()) {
                restoreDatabaseBackup(context, uris)
            }
        }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Ações Diárias", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            // BOTÕES PRINCIPAIS (Destaque total)
            Button(
                onClick = onNavigateToAddSale,
                modifier = Modifier.fillMaxWidth().height(60.dp) // Botão mais alto para clicar fácil
            ) {
                Icon(Icons.Default.AttachMoney, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar Nova Venda", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onNavigateToStats,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.Analytics, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Estatísticas e Relatórios", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider() // Linha separadora
            Spacer(modifier = Modifier.height(8.dp))

            Text("Cadastros Básicos", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            OutlinedButton(
                onClick = onNavigateToClientList,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Meus Clientes (Catálogo e Rotas)")
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text("Segurança de Dados", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)

            // NOVO BOTÃO DE BACKUP
            Button(
                onClick = { exportDatabaseBackup(context) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // Um botão vermelho para destacar
            ) {
                Icon(Icons.Default.Email, contentDescription = "Backup")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exportar Backup (E-mail/Drive)")
            }
            OutlinedButton(
                onClick = {
                    // Abre a tela para selecionar qualquer tipo de arquivo (*/*)
                    restoreLauncher.launch(arrayOf("*/*"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Restore, contentDescription = "Restaurar", tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Importar Backup (Restaurar Dados)", color = MaterialTheme.colorScheme.error)
            }
            // BOTÕES SECUNDÁRIOS (Menos destaque, com bordas)
            OutlinedButton(
                onClick = onNavigateToAddClient,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Novo Cliente")
            }

            OutlinedButton(
                onClick = onNavigateToAddNeighborhood,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Novo Bairro")
            }
        }
    }
}