package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import com.example.mobilepaint.Utils.toPx
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CustomLine(
    private val handlePaint: Paint,
    private val selectionShader: Shader?,
    override val paint: Paint
) : RectF(), Shape {

    private val startPoint = PointF()
    private val endPoint = PointF()

    private var selected = false

    private val handleRadius = 8.toPx

    private var startPointMoving = false
    private var endPointMoving = false

    override fun down(x: Float, y: Float) {
        if (selected) {
            startPointMoving = abs(x - startPoint.x) < handleRadius && abs(y - startPoint.y) < handleRadius
            endPointMoving = abs(x - endPoint.x) < handleRadius && abs(y - endPoint.y) < handleRadius
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

    override fun up(x: Float, y: Float) {
        calculateCoordinates()
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

    override fun getBoundingBox() = this

    override fun isInside(x: Float, y: Float) = contains(x, y)

    override fun setSelected(selected: Boolean) {
        this.selected = selected
        paint.shader = if (selected) selectionShader else null
    }

}