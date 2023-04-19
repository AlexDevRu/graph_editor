package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import android.util.Log
import androidx.core.graphics.values
import com.example.mobilepaint.models.SelectionBorderOptions
import com.example.mobilepaint.drawing_view.Operation
import com.example.mobilepaint.models.json.PathData
import com.example.mobilepaint.models.json.PointData
import com.example.mobilepaint.models.json.ShapeData
import com.google.gson.Gson

class CustomPath(
    selectionBorderOptions: SelectionBorderOptions,
    private val selectionShader: Shader?,
    override val paint: Paint
): Path(), Shape, SelectionBorder.Listener {

    private var selected = false

    private val bounds = RectF()

    private val selectionBorder = SelectionBorder(selectionBorderOptions, this)

    private val matrix = Matrix()
    private val matrix1 = Matrix()

    private val points = mutableListOf<PointData>()

    override fun onTransform(matrix: Matrix) {
        this.matrix1.setConcat(this.matrix1, matrix)
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
            points.add(PointData(x, y))
            moveTo(x, y)
        }
    }

    override fun move(x: Float, y: Float) {
        Log.d(TAG, "move: x=$x")
        Log.d(TAG, "move: y=$y")
        if (selected) {
            selectionBorder.move(x, y)
        } else {
            points.add(PointData(x, y))
            lineTo(x, y)
        }
    }

    private var firstTimeUp = true

    override fun up(x: Float, y: Float) : Operation? {
        computeBounds(bounds, true)
        selectionBorder.up(bounds)
        val operation = when {
            firstTimeUp -> Operation.Creation(this)
            !matrix1.isIdentity -> {
                val inverse = Matrix()
                matrix1.invert(inverse)
                matrix1.reset()
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
            val inverse = Matrix()
            operation.matrix.invert(inverse)
            return Operation.Transformation(this, inverse)
        }
        return null
    }

    override fun toJson(gson: Gson): String {
        val pathData = PathData(
            shapeData = ShapeData(paint.color, null, paint.strokeWidth),
            points = points,
            matrix = matrix.values()
        )
        return gson.toJson(pathData)
    }

    fun addData(pathData: PathData) {
        paint.color = pathData.shapeData.color
        paint.strokeWidth = pathData.shapeData.stroke

        pathData.points.forEachIndexed { index, pointData ->
            Log.d(TAG, "addData: index=$index, x=${pointData.x}, y=${pointData.y}")
            if (index == 0) down(pointData.x, pointData.y)
            else move(pointData.x, pointData.y)
        }

        matrix.setValues(pathData.matrix)
        selectionBorder.applyMatrix(matrix)
        transform(matrix)

        up(0f, 0f)
    }

    override fun changeStrokeWidth(stroke: Float) {
        paint.strokeWidth = stroke
    }

    companion object {
        private const val TAG = "CustomPath"
    }
}