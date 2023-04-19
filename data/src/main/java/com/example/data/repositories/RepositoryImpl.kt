package com.example.data.repositories

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.domain.repositories.Repository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : Repository {

    override suspend fun saveJson(json: String) = withContext(Dispatchers.IO) {
        try {
            val dir = createAndGetAppDir()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH)
            val fileName = dateFormat.format(Date())
            val file = File(dir, "$fileName.json")
            file.writeText(json)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createAndGetAppDir() : File {
        val dir = File(Environment.getExternalStorageDirectory(), "MobilePaint")
        if (!dir.exists())
            dir.mkdirs()
        return dir
    }

}