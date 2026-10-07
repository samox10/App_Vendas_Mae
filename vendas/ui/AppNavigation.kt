package com.samuelsott.vendas.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.samuelsott.vendas.ui.addsale.AddSaleScreen
import com.samuelsott.vendas.ui.admin.AdminMenuScreen
import com.samuelsott.vendas.ui.admin.ClientScreen
import com.samuelsott.vendas.ui.admin.NeighborhoodScreen
import com.samuelsott.vendas.ui.history.HistoryScreen
import com.samuelsott.vendas.ui.editsale.EditSaleScreen
import com.samuelsott.vendas.ui.stats.StatsScreen
import com.samuelsott.vendas.ui.clientlist.ClientListScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun VendasAppNavigation(navController: NavHostController = rememberNavController()) {

    NavHost(
        navController = navController,
        startDestination = "history" // Agora o aplicativo abre direto no Dashboard!
    ) {

        // Rota Principal: Dashboard / Histórico
        composable("history") {
            HistoryScreen(
                onNavigateToAdmin = { navController.navigate("admin_menu") },
                onNavigateToEditSale = { saleId -> navController.navigate("edit_sale/$saleId") } // <--- ROTA NOVA AQUI
            )
        }

        // Rota: Menu de Administração (onde ficam os botões de adicionar)
        composable("admin_menu") {
            AdminMenuScreen(
                onNavigateToAddNeighborhood = { navController.navigate("add_neighborhood") },
                onNavigateToAddClient = { navController.navigate("add_client") },
                onNavigateToAddSale = { navController.navigate("add_sale") },
                onNavigateToStats = { navController.navigate("stats") },
                onNavigateToClientList = { navController.navigate("client_list") } // <--- ADICIONADO
            )
        }
        composable("client_list") {
            ClientListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditClient = { clientId -> navController.navigate("add_client?clientId=$clientId") }
            )
        }
        // Rota 6: Estatísticas
        composable("stats") {
            StatsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("add_neighborhood") {
            NeighborhoodScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = "add_client?clientId={clientId}",
            arguments = listOf(navArgument("clientId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getInt("clientId")
            ClientScreen(
                onNavigateBack = { navController.popBackStack() },
                clientId = clientId
            )
        }

        composable("add_sale") {
            AddSaleScreen(onNavigateBack = { navController.popBackStack() })
        }
        // Rota 5: Editar Venda (Vamos criar a tela no próximo passo!)
        composable("edit_sale/{saleId}") { backStackEntry ->
            val saleId = backStackEntry.arguments?.getString("saleId")?.toIntOrNull()
            if (saleId != null) {
                EditSaleScreen(
                    saleId = saleId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}