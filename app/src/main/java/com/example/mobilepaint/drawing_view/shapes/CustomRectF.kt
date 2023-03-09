package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import kotlin.math.max
import kotlin.math.min

class CustomRectF(
    override val paint: Paint
) : RectF(), Shape {

    private val startPoint = PointF()

    constructor(paint: Paint, rect: RectF) : this(paint) {
        down(rect.left, rect.top)
        move(rect.right, rect.bottom)
    }

    override fun drawInCanvas(canvas: Canvas) {
        canvas.drawRect(this, paint)
    }

    override fun down(x: Float, y: Float) {
        startPoint.x = x
        startPoint.y = y
        calculateCoordinates(x, y)
    }

    override fun move(x: Float, y: Float) {
        calculateCoordinates(x, y)
    }

    private fun calculateCoordinates(x: Float, y: Float) {
        left = min(x, startPoint.x)
        top = min(y, startPoint.y)
        right = max(x, startPoint.x)
        bottom = max(y, startPoint.y)
    }

    override fun isInside(x: Float, y: Float): Boolean {
        return contains(x, y)
    }

    override fun getBoundingBox(): RectF {
        return this
    }

    override fun translate(dx: Float, dy: Float) {
        left += dx
        right += dx
        top += dy
        bottom += dy
    }
}