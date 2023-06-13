package com.pcandroiddev.noteworthyapp.util.downloader.image

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri
import com.pcandroiddev.noteworthyapp.util.downloader.Downloader

class ImageDownloader(
    private val context: Context
) : Downloader {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    override fun downloadFile(url: String): Long {
        val fileName = extractFileNameFromUrl(url = url)

        val request = DownloadManager.Request(url.toUri())
            .setMimeType("image/jpeg")
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(fileName)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        return downloadManager.enqueue(request)
    }

    private fun extractFileNameFromUrl(url: String): String {
        val lastSlashIndex = url.lastIndexOf('/')
        return url.substring(lastSlashIndex + 1)
    }
}
