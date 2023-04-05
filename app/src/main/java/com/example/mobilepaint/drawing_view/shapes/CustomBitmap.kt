package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import android.util.Base64
import android.util.Log
import androidx.core.graphics.values
import com.example.mobilepaint.Utils.isTranslation
import com.example.mobilepaint.drawing_view.Operation
import com.example.mobilepaint.models.SelectionBorderOptions
import com.example.mobilepaint.models.json.BitmapData
import com.example.mobilepaint.models.json.PointData
import com.example.mobilepaint.models.json.ShapeData
import com.google.gson.Gson
import java.io.ByteArrayOutputStream


class CustomBitmap(
    private val bitmap: Bitmap,
    selectionBorderOptions: SelectionBorderOptions,
    override val paint: Paint
): Path(), Shape, SelectionBorder.Listener {

    private val IMAGE_SIZE = bitmap.width
    private val CENTER = IMAGE_SIZE / 2f

    private val selectionBorder = SelectionBorder(selectionBorderOptions, this)

    private var selected = false

    private val bounds = RectF()

    private val matrix1 = Matrix()
    private val matrix2 = Matrix()
    private val matrix3 = Matrix()
    private val matrix4 = Matrix()
    private val matrix5 = Matrix()

    private var x = 0f
    private var y = 0f

    init {
        moveTo(0f, 0f)
        lineTo(IMAGE_SIZE.toFloat(), 0f)
        lineTo(IMAGE_SIZE.toFloat(), IMAGE_SIZE.toFloat())
        lineTo(0f, IMAGE_SIZE.toFloat())
        lineTo(0f, 0f)

        up(0f, 0f)
    }

    override fun onTransform(matrix: Matrix) {
        if (matrix5.isIdentity && !matrix.isTranslation) {
            matrix5.set(matrix2)
        }
        matrix4.setConcat(matrix4, matrix)
        this.matrix1.setConcat(this.matrix1, matrix)
        transform(matrix)
    }

    override fun onScale(sx: Float, sy: Float, rotation: Float) {
        matrix2.reset()
        matrix2.setScale(sx, sy, CENTER, CENTER)
        matrix2.postRotate(rotation, CENTER, CENTER)
        Log.d(TAG, "onScale: matrix=${matrix2.toShortString()}")
    }

    override fun onTranslated(dx: Float, dy: Float) {
        x += dx
        y += dy
    }

    override fun down(x: Float, y: Float) {
        if (selected) {
            selectionBorder.down(x, y)
        }
    }

    override fun move(x: Float, y: Float) {
        if (selected) {
            selectionBorder.move(x, y)
        }
    }

    override fun up(x: Float, y: Float) : Operation? {
        computeBounds(bounds, true)
        Log.e(TAG, "up: bounds=$bounds")
        selectionBorder.up(bounds)
        return when {
            !matrix4.isIdentity -> {
                val matrix4Inverse = Matrix()
                matrix4.invert(matrix4Inverse)
                val matrix5Inverse = Matrix()
                matrix5.invert(matrix5Inverse)
                val operation = Operation.BitmapTransformation(this, matrix4Inverse, matrix5Inverse)
                matrix4.reset()
                matrix5.reset()
                operation
            }
            else -> null
        }
    }

    override fun drawInCanvas(canvas: Canvas) {
        canvas.save()
        canvas.translate(x, y)
        canvas.drawBitmap(bitmap, matrix2, null)
        canvas.restore()
        if (selected) {
            selectionBorder.drawInCanvas(canvas)
        }
    }

    override fun isInside(x: Float, y: Float) = bounds.contains(x, y)

    override fun setSelected(selected: Boolean) {
        this.selected = selected
    }

    override fun applyOperation(operation: Operation): Operation? {
        return when (operation) {
            is Operation.BitmapTransformation -> {
                if (operation.matrix.isTranslation) {
                    val values = operation.matrix.values()
                    x += values[Matrix.MTRANS_X]
                    y += values[Matrix.MTRANS_Y]
                } else {
                    matrix2.setValues(operation.bitmapMatrix.values())
                }
                selectionBorder.applyMatrix(operation.matrix)
                transform(operation.matrix)
                computeBounds(bounds, true)
                val matrix4Inverse = Matrix()
                operation.matrix.invert(matrix4Inverse)
                val matrix5Inverse = Matrix()
                operation.bitmapMatrix.invert(matrix5Inverse)
                val invert = Operation.BitmapTransformation(this, matrix4Inverse, matrix5Inverse)
                invert
            }
            else -> null
        }
    }

    override fun toJson(gson: Gson): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val base64 = Base64.encodeToString(byteArray, Base64.DEFAULT)

        val bitmapData = BitmapData(
            shapeData = ShapeData(paint.color, null, paint.strokeWidth),
            base64 = base64,
            matrix1 = matrix1.values(),
            matrix2 = matrix2.values(),
            point = PointData(x, y),
            sx = selectionBorder.sx,
            sy = selectionBorder.sy,
            rotation = selectionBorder.rotation,
        )

        return gson.toJson(bitmapData)
    }

    fun addData(bitmapData: BitmapData) {
        x = bitmapData.point.x
        y = bitmapData.point.y

        selectionBorder.sx = bitmapData.sx
        selectionBorder.sy = bitmapData.sy
        selectionBorder.rotation = bitmapData.rotation

        matrix1.setValues(bitmapData.matrix1)

        up(0f, 0f)

        matrix2.setValues(bitmapData.matrix2)
        matrix3.setValues(matrix2.values())
        matrix3.postTranslate(x, y)
        selectionBorder.applyMatrix(matrix3)
        transform(matrix3)

        up(0f, 0f)
    }

    companion object {
        private const val TAG = "BitmapData"
    }

}
