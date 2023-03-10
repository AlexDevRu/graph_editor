package com.example.mobilepaint.drawing_view.shapes

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.max
import kotlin.math.min

open class CustomRectF(override val paint: Paint) : RectF(), Shape {

    private var fillPaint : Paint? = null

    constructor(paint: Paint, rect: RectF) : this(paint) {
        down(rect.left, rect.top)
        move(rect.right, rect.bottom)
    }

    override fun down(x: Float, y: Float) {
        left = x
        top = y
        right = x
        bottom = y
    }

    override fun move(x: Float, y: Float) {
        right = x
        bottom = y
    }

    override fun up(x: Float, y: Float) {
        calculateCoordinates()
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
        canvas.drawRect(this, paint)
        fillPaint?.let {
            canvas.drawRect(this, it)
        }
    }

    override fun applyShader(shader: Shader?) {
        paint.shader = shader
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

    override fun translate(dx: Float, dy: Float) {
        left += dx
        right += dx
        top += dy
        bottom += dy
    }

    override fun getBoundingBox() = this

    override fun isInside(x: Float, y: Float) = contains(x, y)

    override fun resize(dx: Float, dy: Float, handlePosition: Int) {
        when (handlePosition) {
            0 -> {
                left += dx
                top += dy
            }
            1 -> {
                right += dx
                top += dy
            }
            2 -> {
                left += dx
                bottom += dy
            }
            3 -> {
                right += dx
                bottom += dy
            }
        }
    }

}