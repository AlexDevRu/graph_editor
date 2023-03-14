package com.example.mobilepaint.drawing_view.shapes

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import com.example.mobilepaint.Utils.toPx
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class CustomRectF(
    protected val handlePaint : Paint,
    protected val boundingBoxPaint : Paint,
    private val selectionShader: Shader?,
    override val paint: Paint
) : RectF(), Shape {

    private var fillPaint : Paint? = null

    protected var selected1 = false

    protected val handleRadius = 8.toPx

    protected var tlPointMoving = false
    protected var trPointMoving = false
    protected var blPointMoving = false
    protected var brPointMoving = false

    protected var isTranslating = false
    private var startX = 0f
    private var startY = 0f

    override fun down(x: Float, y: Float) {
        if (selected1) {
            tlPointMoving = abs(x - left) < handleRadius && abs(y - top) < handleRadius
            trPointMoving = abs(x - right) < handleRadius && abs(y - top) < handleRadius
            blPointMoving = abs(x - left) < handleRadius && abs(y - bottom) < handleRadius
            brPointMoving = abs(x - right) < handleRadius && abs(y - bottom) < handleRadius
            isTranslating = !tlPointMoving && !trPointMoving && !blPointMoving && !brPointMoving && isInside(x, y)
            if (isTranslating) {
                startX = x
                startY = y
            }
        } else {
            left = x
            top = y
            right = x
            bottom = y
        }
    }

    override fun move(x: Float, y: Float) {
        if (selected1) {
            when {
                tlPointMoving -> {
                    left = x
                    top = y
                }
                trPointMoving -> {
                    right = x
                    top = y
                }
                blPointMoving -> {
                    left = x
                    bottom = y
                }
                brPointMoving -> {
                    right = x
                    bottom = y
                }
                isTranslating -> {
                    val dx = x - startX
                    val dy = y - startY
                    left += dx
                    right += dx
                    top += dy
                    bottom += dy
                    startX = x
                    startY = y
                }
            }
        } else {
            right = x
            bottom = y
        }
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
        if (selected1) {
            canvas.drawRect(this, boundingBoxPaint)
            canvas.drawCircle(left, top, handleRadius, handlePaint)
            canvas.drawCircle(right, top, handleRadius, handlePaint)
            canvas.drawCircle(left, bottom, handleRadius, handlePaint)
            canvas.drawCircle(right, bottom, handleRadius, handlePaint)
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

    override fun translate(dx: Float, dy: Float) {
        left += dx
        right += dx
        top += dy
        bottom += dy
    }

    override fun getBoundingBox() = this

    override fun isInside(x: Float, y: Float) = contains(x, y)

    override fun setSelected(selected: Boolean) {
        this.selected1 = selected
        paint.shader = if (selected) selectionShader else null
        tlPointMoving = false
        trPointMoving = false
        blPointMoving = false
        brPointMoving = false
        isTranslating = false
    }

}