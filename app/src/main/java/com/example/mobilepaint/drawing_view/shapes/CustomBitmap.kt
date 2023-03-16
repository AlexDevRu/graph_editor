package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import com.example.mobilepaint.SelectionBorderOptions

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

    private var matrix = Matrix()

    init {
        moveTo(0f, 0f)
        lineTo(imageSize.toFloat(), imageSize.toFloat())
        bounds.set(0f, 0f, imageSize.toFloat(), imageSize.toFloat())
        selectionBorder.up(bounds)
    }

    override fun onTransform(matrix: Matrix) {
        this.matrix.setConcat(this.matrix, matrix)
        //scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        transform(matrix)
        /*computeBounds(bounds, true)
        selectionBorder.up(bounds)*/
    }

    /*override fun onScale(matrix: Matrix) {
        this.matrix.setConcat(this.matrix, matrix)
        //this.matrix = matrix
        //scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        transform(matrix)
    }*/

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

    override fun up(x: Float, y: Float) {
        computeBounds(bounds, true)
        selectionBorder.up(bounds)
    }

    override fun drawInCanvas(canvas: Canvas) {
        canvas.drawBitmap(scaledBitmap, matrix, paint)
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

}
