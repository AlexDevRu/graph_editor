package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import com.example.mobilepaint.Utils.toPx
import com.example.mobilepaint.drawing_view.Operation
import com.example.mobilepaint.models.json.LineData
import com.example.mobilepaint.models.json.PointData
import com.example.mobilepaint.models.json.ShapeData
import com.google.gson.Gson
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CustomLine(
    private val handlePaint: Paint,
    private val selectionShader: Shader?,
    val paint: Paint
) : RectF(), Shape {

    private val startPoint = PointF()
    private val endPoint = PointF()

    private var selected = false

    private val handleRadius = 8.toPx

    private var startPointMoving = false
    private var endPointMoving = false

    private var startX = 0f
    private var startY = 0f

    override fun down(x: Float, y: Float) {
        if (selected) {
            startPointMoving = abs(x - startPoint.x) < handleRadius && abs(y - startPoint.y) < handleRadius
            endPointMoving = abs(x - endPoint.x) < handleRadius && abs(y - endPoint.y) < handleRadius
            if (startPointMoving) {
                startX = startPoint.x
                startY = startPoint.y
            } else if (endPointMoving) {
                startX = endPoint.x
                startY = endPoint.y
            } else {
                startX = 0f
                startY = 0f
            }
        } else {
            startPoint.x = x
            startPoint.y = y
            endPoint.x = x
            endPoint.y = y
        }
    }

    override fun move(x: Float, y: Float) {
        if (selected) {
            if (startPointMoving) {
                startPoint.x = x
                startPoint.y = y
            } else if (endPointMoving) {
                endPoint.x = x
                endPoint.y = y
            }
        } else {
            endPoint.x = x
            endPoint.y = y
        }
    }

    private var firstTimeUp = true

    override fun up(x: Float, y: Float) : Operation? {
        calculateCoordinates()
        return when {
            startPointMoving -> Operation.PointMoving(this, true, startX, startY)
            endPointMoving -> Operation.PointMoving(this, false, startX, startY)
            firstTimeUp -> {
                firstTimeUp = false
                Operation.Creation(this)
            }
            else -> null
        }
    }

    private fun calculateCoordinates() {
        val xMin = min(startPoint.x, endPoint.x)
        val xMax = max(startPoint.x, endPoint.x)
        val yMin = min(startPoint.y, endPoint.y)
        val yMax = max(startPoint.y, endPoint.y)
        left = xMin
        top = yMin
        right = xMax
        bottom = yMax
    }

    override fun drawInCanvas(canvas: Canvas) {
        canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, paint)
        if (selected) {
            canvas.drawCircle(startPoint.x, startPoint.y, handleRadius, handlePaint)
            canvas.drawCircle(endPoint.x, endPoint.y, handleRadius, handlePaint)
        }
    }

    override fun changeColor(color: Int) {
        paint.color = color
    }

    override fun isInside(x: Float, y: Float) = contains(x, y)

    override fun setSelected(selected: Boolean) {
        this.selected = selected
        paint.shader = if (selected) selectionShader else null
    }

    override fun applyOperation(operation: Operation) : Operation? {
        if (operation is Operation.PointMoving) {
            val x: Float
            val y: Float
            if (operation.isStartPoint) {
                x = startPoint.x
                y = startPoint.y
                startPoint.x = operation.x
                startPoint.y = operation.y
            } else {
                x = endPoint.x
                y = endPoint.y
                endPoint.x = operation.x
                endPoint.y = operation.y
            }
            return Operation.PointMoving(this, operation.isStartPoint, x, y)
        }
        return null
    }

    override fun toJson(gson: Gson): String {
        val lineData = LineData(
            shapeData = ShapeData(paint.color, null, paint.strokeWidth),
            start = PointData.fromPoint(startPoint),
            end = PointData.fromPoint(endPoint),
        )
        return gson.toJson(lineData)
    }

    override fun changeStrokeWidth(stroke: Float) {
        paint.strokeWidth = stroke
    }

    fun addData(lineData: LineData) {
        paint.color = lineData.shapeData.color
        paint.strokeWidth = lineData.shapeData.stroke
        down(lineData.start.x, lineData.start.y)
        move(lineData.end.x, lineData.end.y)
        up(lineData.end.x, lineData.end.y)
    }

}