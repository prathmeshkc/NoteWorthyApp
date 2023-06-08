package com.pcandroiddev.noteworthyapp.util.downloader

interface Downloader {

    fun downloadFile(url: String): Long

}