package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import android.util.Log
import com.example.mobilepaint.SelectionBorderOptions
import com.example.mobilepaint.drawing_view.Operation

class CustomPath(
    selectionBorderOptions: SelectionBorderOptions,
    private val selectionShader: Shader?,
    override val paint: Paint
): Path(), Shape, SelectionBorder.Listener {

    private var selected = false

    private val bounds = RectF()

    private val selectionBorder = SelectionBorder(selectionBorderOptions, this)

    private val matrix = Matrix()
    private val inverse = Matrix()

    override fun onTransform(matrix: Matrix) {
        this.matrix.setConcat(this.matrix, matrix)
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

    private var firstTimeUp = true

    override fun up(x: Float, y: Float) : Operation? {
        computeBounds(bounds, true)
        selectionBorder.up(bounds)
        val operation = when {
            firstTimeUp -> Operation.Creation(this)
            !matrix.isIdentity -> {
                matrix.invert(inverse)
                matrix.reset()
                Operation.Transformation(this, inverse)
            }
            else -> null
        }
        firstTimeUp = false
        return operation
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

    override fun applyOperation(operation: Operation): Operation? {
        if (operation is Operation.Transformation) {
            selectionBorder.applyMatrix(operation.matrix)
            transform(operation.matrix)
            computeBounds(bounds, true)
            selectionBorder.up(bounds)
            operation.matrix.invert(inverse)
            return Operation.Transformation(this, inverse)
        }
        return null
    }

    companion object {
        private const val TAG = "CustomPath"
    }
}