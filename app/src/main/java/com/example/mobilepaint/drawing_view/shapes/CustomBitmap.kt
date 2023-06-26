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
    selectionBorderOptions: SelectionBorderOptions
): Path(), Shape, SelectionBorder.Listener {

    private val IMAGE_SIZE = bitmap.width
    private val CENTER = IMAGE_SIZE / 2f

    private val selectionBorder = SelectionBorder(selectionBorderOptions, this)

    private var selected = false

    private val bounds = RectF()

    private val matrix1 = Matrix()
    private val matrix2 = Matrix()
    private val matrix3 = Matrix()

    private var x = 0f
    private var y = 0f

    init {
        reset()
        addRect(0f, 0f, IMAGE_SIZE.toFloat(), IMAGE_SIZE.toFloat(), Direction.CW)
        computeBounds(bounds, true)
        selectionBorder.up(bounds)
    }

    override fun onTransform(matrix: Matrix) {
        /*if (matrix5.isIdentity && !matrix.isTranslation) {
            matrix5.set(matrix2)
        }
        matrix4.setConcat(matrix4, matrix)
        this.matrix1.setConcat(this.matrix1, matrix)*/
        transform(matrix)
    }

    private var sx = 1f
    private var sy = 1f
    private var rotation = 0f
    private var osx = 1f
    private var osy = 1f
    private var orotation = 0f
    private var ox = 0f
    private var oy = 0f

    override fun onScale(sx: Float, sy: Float, rotation: Float) {
        matrix2.reset()
        matrix2.setScale(sx, sy, CENTER, CENTER)
        matrix2.postRotate(rotation, CENTER, CENTER)
        Log.d(TAG, "onScale: matrix=${matrix2.toShortString()}")
        this.sx = sx
        this.sy = sy
        this.rotation = rotation
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
            sx != osx || sy != osy || rotation != orotation || this.x != ox || this.y != oy -> {
                val operation = Operation.BitmapTransformation1(this, ox, oy, osx, osy, orotation)
                osx = sx
                osy = sy
                orotation = rotation
                ox = this.x
                oy = this.y
                operation
            }
            else -> null
        }
    }

    override fun drawInCanvas(canvas: Canvas) {
        canvas.save()
        canvas.translate(x, y)
        canvas.scale(sx, sy, CENTER, CENTER)
        canvas.rotate(rotation, CENTER, CENTER)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
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
            is Operation.BitmapTransformation1 -> {
                val invert = Operation.BitmapTransformation1(this, x, y, sx, sy, rotation)
                sx = operation.sx
                sy = operation.sy
                rotation = operation.rotation
                x = operation.x
                y = operation.y
                Log.d(TAG, "applyOperation: sx=$sx sy=$sy rotation=$rotation x=$x y=$y")

                reset()
                addRect(0f, 0f, IMAGE_SIZE.toFloat(), IMAGE_SIZE.toFloat(), Direction.CW)
                val matrix = Matrix().apply {
                    setTranslate(x, y)
                    postScale(sx, sy, x + CENTER, y + CENTER)
                    postRotate(rotation, x + CENTER, y + CENTER)
                }
                transform(matrix)
                val bounds1 = RectF(0f, 0f, IMAGE_SIZE.toFloat(), IMAGE_SIZE.toFloat())
                selectionBorder.resetAndApplyMatrix(bounds1, matrix, sx, sy, rotation)
                computeBounds(bounds, true)
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
            shapeData = ShapeData(0, null, 0f),
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

        onScale(bitmapData.sx, bitmapData.sy, bitmapData.rotation)

        up(0f, 0f)
    }

    companion object {
        private const val TAG = "BitmapData"
    }

}
