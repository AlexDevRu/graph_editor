package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import android.util.Log
import com.example.mobilepaint.Utils.toPx
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

class CustomEllipse(
    private val handlePaint : Paint,
    private val boundingBoxPaint : Paint,
    private val selectionShader: Shader?,
    override val paint: Paint
) : RectF(), Shape {

    private val bounds1 = RectF()

    private var fillPaint : Paint? = null

    private var selected = false

    private val handleRadius = 8.toPx

    private var tlPointMoving = false
    private var trPointMoving = false
    private var blPointMoving = false
    private var brPointMoving = false
    private var rotationPointMoving = false

    private val handlePoints = FloatArray(10)
    private val boundsPath = Path()

    private val initialBounds = RectF()

    private var isTranslating = false
    private var startX = 0f
    private var startY = 0f

    private var rotation = 0f
    private var sx = 1f
    private var sy = 1f

    private val rotateHandlePaint = Paint(handlePaint).apply {
        color = Color.GREEN
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
            left = x
            top = y
            right = x
            bottom = y
        }
    }

    private val translateMatrix = Matrix()
    private val scaleMatrix = Matrix()
    private val rotateMatrix = Matrix()

    private fun scale(sx: Float, sy: Float, px: Float, py: Float) {
        scaleMatrix.reset()
        scaleMatrix.setScale(sx, sy, px, py)
        boundsPath.transform(scaleMatrix)
        scaleMatrix.mapPoints(handlePoints)
    }

    private fun rotate(degrees: Float) {
        rotateMatrix.reset()
        rotateMatrix.setRotate(degrees, centerX(), centerY())
        boundsPath.transform(rotateMatrix)
        rotateMatrix.mapPoints(handlePoints)
    }

    val bounds3 = RectF()

    override fun move(x: Float, y: Float) {
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

            boundsPath.computeBounds(bounds3, true)

            when {
                /*tlPointMoving -> scale(newSx, newSy, handlePoints[4], handlePoints[5])
                trPointMoving -> scale(newSx, newSy, handlePoints[6], handlePoints[7])
                blPointMoving -> scale(newSx, newSy, handlePoints[2], handlePoints[3])
                brPointMoving -> scale(newSx, newSy, handlePoints[0], handlePoints[1])*/
                tlPointMoving -> scale(newSx, newSy, right, bottom)
                trPointMoving -> scale(newSx, newSy, left, bottom)
                blPointMoving -> scale(newSx, newSy, right, top)
                //brPointMoving -> scale(newSx, newSy, left, top)
                brPointMoving -> scale(newSx, newSy, bounds3.centerX(), bounds3.centerY())
                isTranslating -> {
                    translateMatrix.setTranslate(dx, dy)
                    translateMatrix.mapPoints(handlePoints)
                    boundsPath.transform(translateMatrix)
                }
            }

            startX = x
            startY = y

            this.sx = sx
            this.sy = sy

            set(bounds1)

            Log.d(TAG, "move: bounds=$this")
            Log.d(TAG, "=============================")
        } else {
            right = x
            bottom = y
        }
    }

    override fun up(x: Float, y: Float) {
        calculateCoordinates()
        if (initialBounds.left == 0f) {
            initialBounds.set(this)
            bounds1.set(this)

            handlePoints[0] = left
            handlePoints[1] = top
            handlePoints[2] = right
            handlePoints[3] = top
            handlePoints[4] = right
            handlePoints[5] = bottom
            handlePoints[6] = left
            handlePoints[7] = bottom
            handlePoints[8] = centerX()
            handlePoints[9] = top - 20.toPx

            boundsPath.reset()
            boundsPath.addOval(this, Path.Direction.CW)
        }
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
        if (boundsPath.isEmpty)
            canvas.drawOval(this, paint)
        else
            canvas.drawPath(boundsPath, paint)

        fillPaint?.let {
            canvas.drawPath(boundsPath, it)
        }

        if (selected) {
            canvas.drawPath(boundsPath, boundingBoxPaint)
            canvas.drawCircle(handlePoints[8], handlePoints[9], handleRadius, rotateHandlePaint)
            canvas.drawCircle(handlePoints[0], handlePoints[1], handleRadius, handlePaint)
            canvas.drawCircle(handlePoints[2], handlePoints[3], handleRadius, handlePaint)
            canvas.drawCircle(handlePoints[4], handlePoints[5], handleRadius, handlePaint)
            canvas.drawCircle(handlePoints[6], handlePoints[7], handleRadius, handlePaint)
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

    override fun getBoundingBox() = this

    override fun isInside(x: Float, y: Float) = contains(x, y)

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
        private const val TAG = "CustomEllipse"
    }

}