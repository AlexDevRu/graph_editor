package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import androidx.core.graphics.values
import com.example.mobilepaint.models.SelectionBorderOptions
import com.example.mobilepaint.drawing_view.Operation
import com.example.mobilepaint.models.json.RectData
import com.example.mobilepaint.models.json.ShapeData
import com.google.gson.Gson

class CustomEllipse(
    selectionBorderOptions: SelectionBorderOptions,
    private val selectionShader: Shader?,
    val paint: Paint
) : RectF(), Shape, SelectionBorder.Listener {

    private var fillPaint : Paint? = null

    private var selected = false

    private val selectionBorder = SelectionBorder(selectionBorderOptions, this)

    private val shape = Path()
    private val bounds = RectF()

    private val matrix = Matrix()
    private val matrix1 = Matrix()

    override fun onTransform(matrix: Matrix) {
        this.matrix.setConcat(this.matrix, matrix)
        this.matrix1.setConcat(this.matrix1, matrix)
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
            !matrix1.isIdentity -> {
                val inverse = Matrix()
                matrix1.invert(inverse)
                matrix1.reset()
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
            val inverse = Matrix()
            operation.matrix.invert(inverse)
            return Operation.Transformation(this, inverse)
        }
        return null
    }

    override fun toJson(gson: Gson): String {
        val rectData = RectData(
            shapeData = ShapeData(paint.color, fillPaint?.color, paint.strokeWidth),
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            matrix = matrix.values()
        )
        return gson.toJson(rectData)
    }

    fun addData(rectData: RectData) {
        paint.color = rectData.shapeData.color
        if (rectData.shapeData.fillColor != null)
            fillColor(rectData.shapeData.fillColor)
        paint.strokeWidth = rectData.shapeData.stroke
        matrix.setValues(rectData.matrix)

        down(rectData.left, rectData.top)
        move(rectData.right, rectData.bottom)
        up(rectData.right, rectData.bottom)

        selectionBorder.applyMatrix(matrix)
        shape.transform(matrix)
    }

    override fun changeStrokeWidth(stroke: Float) {
        paint.strokeWidth = stroke
    }

    companion object {
        private const val TAG = "CustomEllipse"
    }

}