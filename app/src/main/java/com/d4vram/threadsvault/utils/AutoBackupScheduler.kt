package com.d4vram.threadsvault.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object AutoBackupScheduler {
    private const val WORK_NAME = "threadsvault_auto_backup"

    fun schedule(context: Context, hours: Int) {
        val periodicHours = if (hours <= 12) 12L else 24L
        val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(
            periodicHours,
            TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}

