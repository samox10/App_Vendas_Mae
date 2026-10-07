package com.samuelsott.vendas

import android.app.Application
import com.samuelsott.vendas.di.AppContainer
import com.samuelsott.vendas.di.DefaultAppContainer

class VendasApplication : Application() {

    // A variável que vai segurar o nosso contêiner
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Assim que o aplicativo abrir, nós iniciamos o contêiner
        container = DefaultAppContainer(this)
    }
}