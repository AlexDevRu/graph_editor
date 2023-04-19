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

    fun saveBitmap(bitmap: Bitmap) {
        val name = generateFileName()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val file = File(dir, "$name.jpeg")
            val outStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            outStream.flush()
            outStream.close()
            return
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }

        val resolver = context.contentResolver
        var uri: Uri? = null

        try {
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Failed to create new MediaStore record.")
            resolver.openOutputStream(uri)?.use {
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it))
                    throw IOException("Failed to save bitmap.")
            } ?: throw IOException("Failed to open output stream.")
        } catch (e: IOException) {
            uri?.let { orphanUri ->
                // Don't leave an orphan entry in the MediaStore
                resolver.delete(orphanUri, null, null)
            }
            throw e
        }
    }

    private fun generateFileName() : String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH)
        return dateFormat.format(Date())
    }

    private fun createAndGetAppDir() : File {
        val dir = File(Environment.getExternalStorageDirectory(), "MobilePaint")
        if (!dir.exists())
            dir.mkdirs()
        return dir
    }

}