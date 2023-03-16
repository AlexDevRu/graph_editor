package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import com.example.mobilepaint.SelectionBorderOptions
import kotlin.math.max
import kotlin.math.min

class CustomEllipse(
    selectionBorderOptions: SelectionBorderOptions,
    private val selectionShader: Shader?,
    override val paint: Paint
) : RectF(), Shape, SelectionBorder.Listener {

    private var fillPaint : Paint? = null

    private var selected = false

    private val selectionBorder = SelectionBorder(selectionBorderOptions, this)

    private val shape = Path()
    private val bounds = RectF()

    override fun onTransform(matrix: Matrix) {
        shape.transform(matrix)
    }

    override fun down(x: Float, y: Float) {
        if (selected) {
            selectionBorder.down(x, y)
        } else {
            left = x
            top = y
            right = x
            bottom = y
        }
    }

    override fun move(x: Float, y: Float) {
        if (selected) {
            selectionBorder.move(x, y)
        } else {
            right = x
            bottom = y
        }
    }

    override fun up(x: Float, y: Float) {
        if (selectionBorder.isEmpty)
            shape.addOval(this, Path.Direction.CW)
        shape.computeBounds(bounds, true)
        selectionBorder.up(bounds)
    }

    private fun calculateCoordinates() {
        val xMin = min(left, right)
        val xMax = max(left, right)
        val yMin = min(top, bottom)
        val yMax = max(top, bottom)
        left = xMin
        top = yMin
        right = xMax
        bottom = yMax
    }

    override fun drawInCanvas(canvas: Canvas) {
        if (shape.isEmpty)
            canvas.drawOval(this, paint)
        else
            canvas.drawPath(shape, paint)

        fillPaint?.let {
            canvas.drawPath(selectionBorder, it)
        }

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
        paint.shader = if (selected) selectionShader else null
    }

    companion object {
        private const val TAG = "CustomEllipse"
    }

}