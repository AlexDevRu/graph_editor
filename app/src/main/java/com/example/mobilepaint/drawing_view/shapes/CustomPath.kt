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
        val invert = Matrix()
        scaleMatrix.invert(invert)
        transform(invert)
        scaleMatrix.setScale(sx, sy, px, py)
        transform(scaleMatrix)
    }

    private var sx = 1f
    private var sy = 1f

    private var initialWidth = 0f
    private var initialHeight = 0f

    override fun move(x: Float, y: Float) {
        if (selected) {
            val newWidth = when {
                tlPointMoving -> 0f
                trPointMoving -> 0f
                blPointMoving -> 0f
                brPointMoving -> x - bounds.left
                else -> 0f
            }
            val newHeight = when {
                tlPointMoving -> 0f
                trPointMoving -> 0f
                blPointMoving -> 0f
                brPointMoving -> y - bounds.top
                else -> 0f
            }

            //val dx = x - startX
            //val dy = y - startY

            //var newWidth = initialWidth + dx
            //var newHeight = initialHeight + dy

            sx = newWidth / initialBounds.width()
            sy = newHeight / initialBounds.height()

            Log.d(TAG, "move: oldWidth=$initialWidth")
            Log.d(TAG, "move: oldHeight=$initialHeight")
            Log.d(TAG, "move: newWidth=$newWidth")
            Log.d(TAG, "move: newHeight=$newHeight")
            Log.d(TAG, "move: sx=$sx")
            Log.d(TAG, "move: sy=$sy")


            when {
                tlPointMoving -> scale(sx, sy, bounds.right, bounds.bottom)
                trPointMoving -> {
                    /*newWidth = initialBounds.width() + dx
                    newHeight = initialBounds.height() - dy
                    sx = newWidth / initialBounds.width()
                    sy = newHeight / initialBounds.height()*/
                    scale(sx, sy, bounds.left, bounds.bottom)
                }
                blPointMoving -> scale(sx, sy, bounds.right, bounds.top)
                brPointMoving -> scale(sx, sy, bounds.left, bounds.top)
                isTranslating -> {
                    val dx = x - startX
                    val dy = y - startY
                    translateMatrix.setTranslate(dx, dy)
                    transform(translateMatrix)
                    startX = x
                    startY = y
                }
            }

            computeBounds(bounds, true)

            Log.d(TAG, "move: bounds=$bounds")
            Log.d(TAG, "=============================")
        } else {
            lineTo(x, y)
        }
    }

    override fun up(x: Float, y: Float) {
        computeBounds(bounds, true)
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

    override fun translate(dx: Float, dy: Float) {

    }

    override fun applyShader(shader: Shader?) {
        paint.shader = shader
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