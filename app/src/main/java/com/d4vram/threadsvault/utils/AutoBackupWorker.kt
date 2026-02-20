package com.d4vram.threadsvault.utils

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.d4vram.threadsvault.data.database.ThreadsVaultDatabase
import com.d4vram.threadsvault.data.preferences.AppPreferences
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AutoBackupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return runCatching {
            val preferences = AppPreferences(applicationContext)
            val folderUri = preferences.autoBackupFolderUriFlow.first().orEmpty()
            if (folderUri.isBlank()) return Result.success()

            val db = ThreadsVaultDatabase.getDatabase(applicationContext)
            val posts = db.postDao().obtenerTodosDirecto()

            val temp = BackupUtils.exportBackupCsv(applicationContext, posts)
            val tree = DocumentFile.fromTreeUri(applicationContext, folderUri.toUri())
                ?: return Result.failure()
            if (!tree.canWrite()) return Result.failure()

            val name = "threadsvault_autobackup_${
                SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            }.csv"
            val target = tree.createFile("text/csv", name) ?: return Result.failure()

            applicationContext.contentResolver.openOutputStream(target.uri)?.use { output ->
                temp.inputStream().use { input -> input.copyTo(output) }
            } ?: return Result.failure()

            temp.delete()
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }
}

