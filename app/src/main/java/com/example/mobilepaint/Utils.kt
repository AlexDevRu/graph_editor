package com.example.mobilepaint

import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.TypedValue
import android.view.View
import androidx.core.graphics.values
import com.example.mobilepaint.drawing_view.DrawingView
import com.example.mobilepaint.models.CanvasData
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object Utils {

    val Number.toPx
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        )

    fun convert(bitmap: Bitmap) : String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun convert(base64: String): Bitmap {
        val decodedBytes = Base64.decode(base64.substring(base64.indexOf(",")  + 1), Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    val Matrix.isTranslation: Boolean
        get() = values()[0] == 1f && values()[1] == 0f && values()[2] != 0f &&
                values()[3] == 0f && values()[4] == 1f && values()[5] != 0f &&
                values()[6] == 0f && values()[7] == 0f && values()[8] == 1f

    fun createAndGetAppDir() : File {
        val dir = File(Environment.getExternalStorageDirectory(), "MobilePaint")
        if (!dir.exists())
            dir.mkdirs()
        return dir
    }

    fun generateFileName() : String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH)
        return dateFormat.format(Date())
    }

    fun saveBitmap(fileName: String, context: Context, canvasData: CanvasData) : String {
        val drawingView = DrawingView(context)
        drawingView.measure(
            View.MeasureSpec.makeMeasureSpec(canvasData.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(canvasData.height, View.MeasureSpec.EXACTLY),
        )
        drawingView.layout(0, 0, canvasData.width, canvasData.height)
        drawingView.addShapes(canvasData.shapesList, canvasData.removedShapesList)
        val bitmap = drawingView.getBitmap()
        val file = File(context.cacheDir, "$fileName.jpg")
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()
        return file.absolutePath
    }

    fun saveBitmap(context: Context, bitmap: Bitmap) {
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

    fun createOrOverwriteJson(text: String, name: String = generateFileName()) : String {
        val directory = createAndGetAppDir()
        val file = File(directory, "$name.json")
        file.writeText(text)
        return name
    }
}