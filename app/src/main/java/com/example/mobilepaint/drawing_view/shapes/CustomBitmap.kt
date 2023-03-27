package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import android.util.Base64
import android.util.Log
import androidx.core.graphics.values
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
    private var matrix4 = Matrix()

    private var x = 0f
    private var y = 0f

    var sumDx = 0f
    var sumDy = 0f

    init {
        moveTo(0f, 0f)
        lineTo(IMAGE_SIZE.toFloat(), 0f)
        lineTo(IMAGE_SIZE.toFloat(), IMAGE_SIZE.toFloat())
        lineTo(0f, IMAGE_SIZE.toFloat())
        lineTo(0f, 0f)

        up(0f, 0f)
    }

    override fun onTransform(matrix: Matrix) {
        matrix4 = matrix
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
        sumDx += dx
        sumDy += dy
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

    private var firstTimeUp = true

    override fun up(x: Float, y: Float) : Operation? {
        computeBounds(bounds, true)
        Log.e(TAG, "up: bounds=$bounds")
        selectionBorder.up(bounds)
        return when {
            firstTimeUp -> {
                firstTimeUp = false
                Operation.Creation(this)
            }
            sumDx > 0f || sumDy > 0f -> {
                val operation = Operation.BitmapTranslation(this, -sumDx, -sumDy)
                sumDx = 0f
                sumDx = 0f
                operation
            }
            !matrix2.isIdentity || !matrix4.isIdentity -> {
                val operation = Operation.BitmapTransformation(this, matrix4, matrix2)
                matrix4.reset()
                matrix2.reset()
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
            is Operation.BitmapTranslation -> {
                x += operation.x
                y += operation.y
                val matrix = Matrix().apply {
                    postTranslate(operation.x, operation.y)
                }
                transform(matrix)
                selectionBorder.applyMatrix(matrix)
                val invert = Operation.BitmapTranslation(this, -operation.x, -operation.y)
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
