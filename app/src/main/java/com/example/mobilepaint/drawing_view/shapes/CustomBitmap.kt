package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import android.util.Log
import androidx.core.graphics.values
import com.example.mobilepaint.SelectionBorderOptions
import com.example.mobilepaint.drawing_view.Operation

class CustomBitmap(
    private val bitmap: Bitmap,
    selectionBorderOptions: SelectionBorderOptions,
    override val paint: Paint
): Path(), Shape, SelectionBorder.Listener {

    private val selectionBorder = SelectionBorder(selectionBorderOptions, this)

    private var fillPaint : Paint? = null

    private var selected = false

    private var imageSize = 300

    private var scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize,true)

    private val bounds = RectF()

    private val matrix = Matrix()
    private val matrix1 = Matrix()

    private var x = 0f
    private var y = 0f

    init {
        moveTo(0f, 0f)
        lineTo(imageSize.toFloat(), imageSize.toFloat())
        bounds.set(0f, 0f, imageSize.toFloat(), imageSize.toFloat())
        selectionBorder.up(bounds)
    }

    override fun onTransform(matrix: Matrix) {
        this.matrix.setConcat(this.matrix, matrix)
        this.matrix1.setConcat(this.matrix1, matrix)
        Log.d(TAG, "onTransform: matrix1=${matrix1.toShortString()}")
        //x = matrix1.values()[2]
        //y = matrix1.values()[5]
        //scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix1, true)
        transform(matrix)

        /*computeBounds(bounds, true)
        selectionBorder.up(bounds)*/
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
        selectionBorder.up(bounds)
        return null
    }

    override fun drawInCanvas(canvas: Canvas) {
        /*canvas.save()
        canvas.translate(x, y)
        canvas.scale(selectionBorder.sx, selectionBorder.sy, selectionBorder.bounds.centerX(), selectionBorder.bounds.centerY())
        canvas.rotate(selectionBorder.rotation, selectionBorder.bounds.centerX(), selectionBorder.bounds.centerY())
        canvas.drawBitmap(scaledBitmap, x, y, paint)
        canvas.restore()*/
        canvas.drawBitmap(scaledBitmap, matrix1, paint)
        if (selected) {
            selectionBorder.drawInCanvas(canvas)
        }
    }

    override fun changeColor(color: Int) {
        paint.color = color
    }

    override fun fillColor(color: Int): Boolean {
        if (fillPaint == null)
            fillPaint = Paint().apply {
                style = Paint.Style.FILL
            }
        fillPaint?.color = color
        return true
    }

    override fun isInside(x: Float, y: Float) = bounds.contains(x, y)

    override fun setSelected(selected: Boolean) {
        this.selected = selected
    }

    companion object {
        private const val TAG = "CustomBitmap"
    }

}
