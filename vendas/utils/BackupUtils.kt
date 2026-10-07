package com.samuelsott.vendas.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

fun exportDatabaseBackup(context: Context) {
    try {
        // 1. Cria uma pasta temporária segura para o backup
        val backupDir = File(context.cacheDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        // 2. Localiza os 3 arquivos originais que compõem o banco de dados moderno do Android
        val dbName = "vendas_database"
        val dbFile = context.getDatabasePath(dbName)
        val dbWalFile = context.getDatabasePath("$dbName-wal")
        val dbShmFile = context.getDatabasePath("$dbName-shm")

        val filesToShare = ArrayList<android.net.Uri>()

        // 3. Função interna para copiar e gerar o link seguro (URI)
        fun copyAndGetUri(sourceFile: File) {
            if (sourceFile.exists()) {
                val destFile = File(backupDir, sourceFile.name)
                sourceFile.copyTo(destFile, overwrite = true)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    destFile
                )
                filesToShare.add(uri)
            }
        }

        // Copia tudo!
        copyAndGetUri(dbFile)
        copyAndGetUri(dbWalFile)
        copyAndGetUri(dbShmFile)

        // 4. Cria o "Pacote" de envio
        if (filesToShare.isNotEmpty()) {
            val dataHoje = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date())

            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "application/octet-stream"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesToShare)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_SUBJECT, "Backup Vendas - $dataHoje")
                putExtra(Intent.EXTRA_TEXT, "Aqui estão os 3 arquivos de backup do aplicativo de Vendas.")
            }
            context.startActivity(Intent.createChooser(intent, "Salvar Backup usando:"))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
fun restoreDatabaseBackup(context: Context, uris: List<Uri>) {
    try {
        val dbName = "vendas_database"
        val dbPath = context.getDatabasePath(dbName)
        val walPath = context.getDatabasePath("$dbName-wal")
        val shmPath = context.getDatabasePath("$dbName-shm")

        var restoredCount = 0

        // Analisa cada arquivo que você selecionou
        uris.forEach { uri ->
            // Descobre o nome original do arquivo
            var name = ""
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    name = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }

            // Verifica qual parte do banco de dados é essa
            val targetFile = when {
                name.contains("wal") -> walPath
                name.contains("shm") -> shmPath
                name.contains(dbName) -> dbPath
                else -> null
            }

            // Substitui o arquivo antigo pelo do backup
            targetFile?.let { dest ->
                context.contentResolver.openInputStream(uri)?.use { input ->
                    dest.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                restoredCount++
            }
        }

        if (restoredCount > 0) {
            Toast.makeText(context, "Backup restaurado! Fechando o app para aplicar...", Toast.LENGTH_LONG).show()
            // Dá 2 segundos para o usuário ler o aviso e "mata" o aplicativo
            Thread.sleep(2000)
            exitProcess(0)
        } else {
            Toast.makeText(context, "Nenhum arquivo válido de backup foi selecionado.", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Erro ao restaurar backup.", Toast.LENGTH_SHORT).show()
    }
}