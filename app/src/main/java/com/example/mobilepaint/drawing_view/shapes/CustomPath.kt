package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import android.util.Log
import androidx.core.graphics.transform
import com.example.mobilepaint.Utils.toPx
import kotlin.math.abs
import kotlin.math.atan2

class CustomPath(
    private val handlePaint : Paint,
    private val boundingBoxPaint : Paint,
    private val selectionShader: Shader?,
    override val paint: Paint
): Path(), Shape {

    private val bounds = RectF()
    private val bounds1 = RectF()
    private val handlePoints = FloatArray(10)

    private val translateMatrix = Matrix()
    private val scaleMatrix = Matrix()
    private val rotateMatrix = Matrix()

    private var selected = false

    private val handleRadius = 8.toPx

    private var tlPointMoving = false
    private var trPointMoving = false
    private var blPointMoving = false
    private var brPointMoving = false
    private var rotationPointMoving = false

    private var isTranslating = false
    private var startX = 0f
    private var startY = 0f

    private var rotation = 0f

    private val initialBounds = RectF(0f, 0f, 0f, 0f)

    private val boundsPath = Path()

    private val rotateHandlePaint = Paint(handlePaint).apply {
        color = Color.GREEN
    }

    private var sx = 1f
    private var sy = 1f

    override fun drawInCanvas(canvas: Canvas) {
        canvas.drawPath(this, paint)
        if (selected) {
            canvas.drawPath(boundsPath, boundingBoxPaint)
            //canvas.drawRect(bounds, boundingBoxPaint)
            canvas.drawCircle(handlePoints[8], handlePoints[9], handleRadius, rotateHandlePaint)
            canvas.drawCircle(handlePoints[0], handlePoints[1], handleRadius, handlePaint)
            canvas.drawCircle(handlePoints[2], handlePoints[3], handleRadius, handlePaint)
            canvas.drawCircle(handlePoints[4], handlePoints[5], handleRadius, handlePaint)
            canvas.drawCircle(handlePoints[6], handlePoints[7], handleRadius, handlePaint)
            /*canvas.drawCircle(bounds.left, bounds.top, handleRadius, handlePaint)
            canvas.drawCircle(bounds.right, bounds.top, handleRadius, handlePaint)
            canvas.drawCircle(bounds.left, bounds.bottom, handleRadius, handlePaint)
            canvas.drawCircle(bounds.right, bounds.bottom, handleRadius, handlePaint)*/
        }
    }

    override fun down(x: Float, y: Float) {
        if (selected) {
            tlPointMoving = abs(x - handlePoints[0]) < handleRadius && abs(y - handlePoints[1]) < handleRadius
            trPointMoving = abs(x - handlePoints[2]) < handleRadius && abs(y - handlePoints[3]) < handleRadius
            blPointMoving = abs(x - handlePoints[6]) < handleRadius && abs(y - handlePoints[7]) < handleRadius
            brPointMoving = abs(x - handlePoints[4]) < handleRadius && abs(y - handlePoints[5]) < handleRadius
            rotationPointMoving = abs(x - handlePoints[8]) < handleRadius && abs(y - handlePoints[9]) < handleRadius
            isTranslating = !tlPointMoving && !trPointMoving && !blPointMoving && !brPointMoving && !rotationPointMoving && isInside(x, y)
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
        boundsPath.transform(scaleMatrix)
        scaleMatrix.mapPoints(handlePoints)
    }

    private fun rotate(degrees: Float) {
        rotateMatrix.reset()
        rotateMatrix.setRotate(degrees, bounds.centerX(), bounds.centerY())
        transform(rotateMatrix)
        boundsPath.transform(rotateMatrix)
        rotateMatrix.mapPoints(handlePoints)
    }

    override fun move(x: Float, y: Float) {
        Log.d(TAG, "move: x=$x")
        Log.d(TAG, "move: y=$y")
        if (selected) {
            if (rotationPointMoving) {
                val xx = x - bounds1.centerX()
                val yy = -(y - bounds1.centerY())
                val newR = 90 - Math.toDegrees(atan2(yy, xx).toDouble()).toFloat()
                val extraRotation = newR - rotation
                Log.d(TAG, "move: newR=$newR")
                Log.d(TAG, "move: rotation=$rotation")
                Log.d(TAG, "move: extraRotation=$extraRotation")
                Log.d(TAG, "=============================")
                rotation = newR
                rotate(extraRotation)
                return
            }

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

            Log.d(TAG, "move: newWidth=$newWidth")
            Log.d(TAG, "move: newHeight=$newHeight")
            Log.d(TAG, "move: sx=$sx")
            Log.d(TAG, "move: sy=$sy")

            when {
                tlPointMoving -> scale(newSx, newSy, handlePoints[4], handlePoints[5])
                trPointMoving -> scale(newSx, newSy, handlePoints[6], handlePoints[7])
                blPointMoving -> scale(newSx, newSy, handlePoints[2], handlePoints[3])
                brPointMoving -> scale(newSx, newSy, handlePoints[0], handlePoints[1])
                isTranslating -> {
                    translateMatrix.setTranslate(dx, dy)
                    transform(translateMatrix)
                    translateMatrix.mapPoints(handlePoints)
                    boundsPath.transform(translateMatrix)
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
        Log.d(TAG, "up: bounds=$bounds")
        Log.d(TAG, "=============================")
        if (initialBounds.left == 0f) {
            initialBounds.set(bounds)
            bounds1.set(bounds)

            handlePoints[0] = bounds.left
            handlePoints[1] = bounds.top
            handlePoints[2] = bounds.right
            handlePoints[3] = bounds.top
            handlePoints[4] = bounds.right
            handlePoints[5] = bounds.bottom
            handlePoints[6] = bounds.left
            handlePoints[7] = bounds.bottom
            handlePoints[8] = bounds.centerX()
            handlePoints[9] = bounds.top - 20.toPx

            boundsPath.reset()
            boundsPath.addRect(bounds, Direction.CW)
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
        rotationPointMoving = false
    }

    companion object {
        private const val TAG = "CustomPath"
    }
}