package com.samuelsott.vendas.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Cria um arquivo temporário em branco e devolve o "endereço" (Uri) seguro dele
fun Context.createImageFileUri(): Uri {
    // Cria um nome único baseado na data e hora atual
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"

    // Cria o arquivo físico na pasta de cache do aplicativo
    val image = File.createTempFile(
        imageFileName,
        ".jpg",
        externalCacheDir
    )

    // Devolve o endereço seguro pelo FileProvider que acabamos de configurar
    return FileProvider.getUriForFile(
        this,
        "${packageName}.fileprovider",
        image
    )
}