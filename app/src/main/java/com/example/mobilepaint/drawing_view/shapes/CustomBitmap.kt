package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import android.util.Log
import com.example.mobilepaint.SelectionBorderOptions
import com.example.mobilepaint.drawing_view.Operation

class CustomBitmap(
    bitmap: Bitmap,
    selectionBorderOptions: SelectionBorderOptions,
    override val paint: Paint
): Path(), Shape, SelectionBorder.Listener {

    private val selectionBorder = SelectionBorder(selectionBorderOptions, this)

    private var selected = false

    private val scaledBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE,true)

    private val bounds = RectF()

    private val matrix = Matrix()

    private var x = 0f
    private var y = 0f

    init {
        moveTo(0f, 0f)
        lineTo(IMAGE_SIZE.toFloat(), 0f)
        lineTo(IMAGE_SIZE.toFloat(), IMAGE_SIZE.toFloat())
        lineTo(0f, IMAGE_SIZE.toFloat())
        close()

        up(0f, 0f)
    }

    override fun onTransform(matrix: Matrix) {
        transform(matrix)
    }

    override fun onScale(sx: Float, sy: Float, rotation: Float) {
        matrix.reset()
        matrix.setScale(sx, sy, CENTER, CENTER)
        matrix.postRotate(rotation, CENTER, CENTER)
        Log.d(TAG, "onScale: matrix=${matrix.toShortString()}")
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
        return null
    }

    override fun drawInCanvas(canvas: Canvas) {
        canvas.save()
        canvas.translate(x, y)
        canvas.drawBitmap(scaledBitmap, matrix, null)
        canvas.restore()
        if (selected) {
            selectionBorder.drawInCanvas(canvas)
        }
    }

    override fun isInside(x: Float, y: Float) = bounds.contains(x, y)

    override fun setSelected(selected: Boolean) {
        this.selected = selected
    }

    companion object {
        private const val TAG = "CustomBitmap"
        private const val IMAGE_SIZE = 300
        private const val CENTER = IMAGE_SIZE / 2f
    }

}
