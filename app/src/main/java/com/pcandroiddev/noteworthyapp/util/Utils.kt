package com.pcandroiddev.noteworthyapp.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class Utils {

    companion object {
        fun getFileNameStamp(): String {
            val currentDateTime = LocalDateTime.now()
            val dateFormat = DateTimeFormatter.ofPattern("MMddyyyy_hhmma", Locale.getDefault())
            return currentDateTime.format(dateFormat)
        }
    }
}