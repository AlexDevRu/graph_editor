package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import com.example.mobilepaint.SelectionBorderOptions
import com.example.mobilepaint.drawing_view.Operation

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

    private val matrix = Matrix()
    private val inverse = Matrix()

    override fun onTransform(matrix: Matrix) {
        this.matrix.setConcat(this.matrix, matrix)
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

    override fun up(x: Float, y: Float) : Operation? {
        val firstTimeUp = shape.isEmpty
        if (selectionBorder.isEmpty)
            shape.addOval(this, Path.Direction.CW)
        shape.computeBounds(bounds, true)
        selectionBorder.up(bounds)
        return when {
            firstTimeUp -> Operation.Creation(this)
            !matrix.isIdentity -> {
                matrix.invert(inverse)
                matrix.reset()
                Operation.Transformation(this, inverse)
            }
            else -> null
        }
    }

    override fun drawInCanvas(canvas: Canvas) {
        if (shape.isEmpty)
            canvas.drawOval(this, paint)
        else
            canvas.drawPath(shape, paint)

        fillPaint?.let {
            canvas.drawPath(shape, it)
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

    override fun applyOperation(operation: Operation): Operation? {
        if (operation is Operation.Transformation) {
            selectionBorder.applyMatrix(operation.matrix)
            shape.transform(operation.matrix)
            shape.computeBounds(bounds, true)
            selectionBorder.up(bounds)
            operation.matrix.invert(inverse)
            return Operation.Transformation(this, inverse)
        }
        return null
    }

    companion object {
        private const val TAG = "CustomEllipse"
    }

}