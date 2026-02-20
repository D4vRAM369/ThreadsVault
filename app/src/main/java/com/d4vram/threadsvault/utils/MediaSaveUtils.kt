package com.d4vram.threadsvault.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

object MediaSaveUtils {

    fun saveToGallery(context: Context, mediaUrl: String): Long {
        val isVideo = MediaUrlUtils.isVideoUrl(mediaUrl)
        val targetDir = if (isVideo) {
            Environment.DIRECTORY_MOVIES
        } else {
            Environment.DIRECTORY_PICTURES
        }
        val fileName = buildFileName(mediaUrl, isVideo)
        val request = DownloadManager.Request(Uri.parse(mediaUrl))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(targetDir, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return downloadManager.enqueue(request)
    }

    private fun buildFileName(url: String, isVideo: Boolean): String {
        val ext = when {
            url.contains(".mp4", ignoreCase = true) -> ".mp4"
            url.contains(".webm", ignoreCase = true) -> ".webm"
            url.contains(".jpg", ignoreCase = true) -> ".jpg"
            url.contains(".jpeg", ignoreCase = true) -> ".jpeg"
            url.contains(".png", ignoreCase = true) -> ".png"
            else -> if (isVideo) ".mp4" else ".jpg"
        }
        return "threadsvault_${System.currentTimeMillis()}$ext"
    }
}
