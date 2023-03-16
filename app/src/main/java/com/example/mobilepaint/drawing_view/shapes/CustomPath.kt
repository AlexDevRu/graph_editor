package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import android.util.Log
import com.example.mobilepaint.SelectionBorderOptions

class CustomPath(
    selectionBorderOptions: SelectionBorderOptions,
    private val selectionShader: Shader?,
    override val paint: Paint
): Path(), Shape, SelectionBorder.Listener {

    private var selected = false

    private val bounds = RectF()

    private val selectionBorder = SelectionBorder(selectionBorderOptions, this)

    override fun onTransform(matrix: Matrix) {
        transform(matrix)
    }

    override fun drawInCanvas(canvas: Canvas) {
        canvas.drawPath(this, paint)
        if (selected) {
            selectionBorder.drawInCanvas(canvas)
        }
    }

    override fun down(x: Float, y: Float) {
        if (selected) {
            selectionBorder.down(x, y)
        } else {
            moveTo(x, y)
        }
    }

    override fun move(x: Float, y: Float) {
        Log.d(TAG, "move: x=$x")
        Log.d(TAG, "move: y=$y")
        if (selected) {
            selectionBorder.move(x, y)
        } else {
            lineTo(x, y)
        }
    }

    override fun up(x: Float, y: Float) {
        computeBounds(bounds, true)
        selectionBorder.up(bounds)
    }

    override fun changeColor(color: Int) {
        paint.color = color
    }

    override fun setSelected(selected: Boolean) {
        this.selected = selected
        paint.shader = if (selected) selectionShader else null
    }

    override fun isInside(x: Float, y: Float): Boolean {
        return bounds.contains(x, y)
    }

    companion object {
        private const val TAG = "CustomPath"
    }
}