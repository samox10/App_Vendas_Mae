package com.samuelsott.vendas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.samuelsott.vendas.ui.VendasAppNavigation
import com.samuelsott.vendas.ui.theme.ControleDeVendasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControleDeVendasTheme {
                // Aqui nós chamamos o roteador que acabamos de criar!
                VendasAppNavigation()
            }
        }
    }
}