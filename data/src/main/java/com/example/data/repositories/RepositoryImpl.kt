package com.example.data.repositories

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.domain.repositories.Repository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : Repository {

    companion object {
        private const val APP_DIR = "MobilePaint"
    }

    override suspend fun saveJson(json: String, fileName: String) = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(dir, APP_DIR)
                if (!appDir.exists())
                    appDir.mkdirs()
                val file = File(appDir, "$fileName.json")
                file.writeText(json)
                return@withContext Result.success(Unit)
            }

            val resolver = context.contentResolver
            var uri: Uri? = null
            try {
                val contentUri = MediaStore.Files.getContentUri("external")
                val cursor = resolver.query(
                    contentUri,
                    null,
                    MediaStore.MediaColumns.RELATIVE_PATH + "=? and " + MediaStore.MediaColumns.DISPLAY_NAME + "=?",
                    arrayOf(Environment.DIRECTORY_DOCUMENTS + "/$APP_DIR", fileName),
                    null
                )

                uri = if (cursor != null && cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    cursor.close()
                    ContentUris.withAppendedId(contentUri, id)
                } else {
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/$APP_DIR")
                        }
                    }
                    resolver.insert(contentUri, values)
                        ?: throw IOException("Failed to create new MediaStore record.")
                }
                resolver.openOutputStream(uri)?.use {
                    it.write(json.encodeToByteArray())
                } ?: throw IOException("Failed to open output stream.")
            } catch (e: IOException) {
                uri?.let { orphanUri ->
                    resolver.delete(orphanUri, null, null)
                }
                throw e
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



}