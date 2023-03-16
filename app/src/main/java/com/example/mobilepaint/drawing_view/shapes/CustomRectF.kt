package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import com.example.mobilepaint.SelectionBorderOptions

class CustomRectF(
    selectionBorderOptions: SelectionBorderOptions,
    private val selectionShader: Shader?,
    override val paint: Paint
) : RectF(), Shape, SelectionBorder.Listener {

    private val selectionBorder = SelectionBorder(selectionBorderOptions, this)

    private var fillPaint : Paint? = null

    private var selected1 = false

    private val shape = Path()
    private val bounds = RectF()

    override fun onTransform(matrix: Matrix) {
        shape.transform(matrix)
    }

    override fun down(x: Float, y: Float) {
        if (selected1) {
            selectionBorder.down(x, y)
        } else {
            left = x
            top = y
            right = x
            bottom = y
        }
    }

    override fun move(x: Float, y: Float) {
        if (selected1) {
            selectionBorder.move(x, y)
        } else {
            right = x
            bottom = y
        }
    }

    override fun up(x: Float, y: Float) {
        if (selectionBorder.isEmpty)
            shape.addRect(this, Path.Direction.CW)
        shape.computeBounds(bounds, true)
        selectionBorder.up(bounds)
    }

    override fun drawInCanvas(canvas: Canvas) {
        if (shape.isEmpty)
            canvas.drawRect(this, paint)
        else
            canvas.drawPath(shape, paint)

        fillPaint?.let {
            canvas.drawPath(shape, it)
        }

        if (selected1) {
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

    override fun isInside(x: Float, y: Float) = contains(x, y)

    override fun setSelected(selected: Boolean) {
        this.selected1 = selected
        paint.shader = if (selected) selectionShader else null
    }

    companion object {
        private const val TAG = "CustomRectF"
    }

}