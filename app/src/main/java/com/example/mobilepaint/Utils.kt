package com.example.mobilepaint

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.Base64
import android.util.TypedValue
import android.view.View
import androidx.core.graphics.values
import java.io.ByteArrayOutputStream

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

    fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    val Matrix.isTranslation: Boolean
        get() = values()[0] == 1f && values()[1] == 0f && values()[2] != 0f &&
                values()[3] == 0f && values()[4] == 1f && values()[5] != 0f &&
                values()[6] == 0f && values()[7] == 0f && values()[8] == 1f
}