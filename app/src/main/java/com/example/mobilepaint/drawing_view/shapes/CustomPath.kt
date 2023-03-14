package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import android.util.Log
import com.example.mobilepaint.Utils.toPx
import kotlin.math.abs

class CustomPath(
    private val handlePaint : Paint,
    private val boundingBoxPaint : Paint,
    private val selectionShader: Shader?,
    override val paint: Paint
): Path(), Shape {

    private val bounds = RectF()
    private val bounds1 = RectF()

    private val translateMatrix = Matrix()
    private val scaleMatrix = Matrix()

    private var selected = false

    private val handleRadius = 8.toPx

    private var tlPointMoving = false
    private var trPointMoving = false
    private var blPointMoving = false
    private var brPointMoving = false

    private var isTranslating = false
    private var startX = 0f
    private var startY = 0f

    private val initialBounds = RectF(0f, 0f, 0f, 0f)

    override fun drawInCanvas(canvas: Canvas) {
        canvas.drawPath(this, paint)
        if (selected) {
            canvas.drawRect(bounds, boundingBoxPaint)
            canvas.drawCircle(bounds.left, bounds.top, handleRadius, handlePaint)
            canvas.drawCircle(bounds.right, bounds.top, handleRadius, handlePaint)
            canvas.drawCircle(bounds.left, bounds.bottom, handleRadius, handlePaint)
            canvas.drawCircle(bounds.right, bounds.bottom, handleRadius, handlePaint)
        }
    }

    override fun down(x: Float, y: Float) {
        if (selected) {
            tlPointMoving = abs(x - bounds.left) < handleRadius && abs(y - bounds.top) < handleRadius
            trPointMoving = abs(x - bounds.right) < handleRadius && abs(y - bounds.top) < handleRadius
            blPointMoving = abs(x - bounds.left) < handleRadius && abs(y - bounds.bottom) < handleRadius
            brPointMoving = abs(x - bounds.right) < handleRadius && abs(y - bounds.bottom) < handleRadius
            isTranslating = !tlPointMoving && !trPointMoving && !blPointMoving && !brPointMoving && isInside(x, y)
            startX = x
            startY = y
        } else {
            moveTo(x, y)
        }
    }

    private fun scale(sx: Float, sy: Float, px: Float, py: Float) {
        scaleMatrix.reset()
        scaleMatrix.setScale(sx, sy, px, py)
        transform(scaleMatrix)
    }

    private var sx = 1f
    private var sy = 1f

    private var initialWidth = 0f
    private var initialHeight = 0f

    override fun move(x: Float, y: Float) {
        Log.d(TAG, "move: x=$x")
        Log.d(TAG, "move: y=$y")
        if (selected) {
            val dx = x - startX
            val dy = y - startY

            when {
                tlPointMoving -> {
                    bounds1.left += dx
                    bounds1.top += dy
                }
                trPointMoving -> {
                    bounds1.right += dx
                    bounds1.top += dy
                }
                blPointMoving -> {
                    bounds1.left += dx
                    bounds1.bottom += dy
                }
                brPointMoving -> {
                    bounds1.right += dx
                    bounds1.bottom += dy
                }
                isTranslating -> {
                    bounds1.left += dx
                    bounds1.right += dx
                    bounds1.top += dy
                    bounds1.bottom += dy
                }
                else -> {}
            }

            val newWidth = bounds1.width()
            val newHeight = bounds1.height()

            val sx = newWidth / initialBounds.width()
            val sy = newHeight / initialBounds.height()
            val newSx = sx / this.sx
            val newSy = sy / this.sy

            Log.d(TAG, "move: oldWidth=$initialWidth")
            Log.d(TAG, "move: oldHeight=$initialHeight")
            Log.d(TAG, "move: newWidth=$newWidth")
            Log.d(TAG, "move: newHeight=$newHeight")
            Log.d(TAG, "move: sx=$sx")
            Log.d(TAG, "move: sy=$sy")

            when {
                tlPointMoving -> scale(newSx, newSy, bounds.right, bounds.bottom)
                trPointMoving -> scale(newSx, newSy, bounds.left, bounds.bottom)
                blPointMoving -> scale(newSx, newSy, bounds.right, bounds.top)
                brPointMoving -> scale(newSx, newSy, bounds.left, bounds.top)
                isTranslating -> {
                    translateMatrix.setTranslate(dx, dy)
                    transform(translateMatrix)
                }
            }

            startX = x
            startY = y

            this.sx = sx
            this.sy = sy

            bounds.set(bounds1)

            Log.d(TAG, "move: bounds=$bounds")
            Log.d(TAG, "=============================")
        } else {
            lineTo(x, y)
        }
    }

    override fun up(x: Float, y: Float) {
        computeBounds(bounds, true)
        bounds1.set(bounds)
        Log.d(TAG, "up: bounds=$bounds")
        Log.d(TAG, "=============================")
        if (initialBounds.left == 0f) {
            initialBounds.set(bounds)
            initialWidth = initialBounds.width()
            initialHeight = initialBounds.height()
        } else {
            initialWidth = bounds.width()
            initialHeight = bounds.height()
        }
    }

    override fun isInside(x: Float, y: Float): Boolean {
        return bounds.contains(x, y)
    }

    override fun getBoundingBox(): RectF {
        return bounds
    }

    override fun changeColor(color: Int) {
        paint.color = color
    }

    override fun setSelected(selected: Boolean) {
        this.selected = selected
        paint.shader = if (selected) selectionShader else null
        tlPointMoving = false
        trPointMoving = false
        blPointMoving = false
        brPointMoving = false
        isTranslating = false
    }

    companion object {
        private const val TAG = "CustomPath"
    }
}