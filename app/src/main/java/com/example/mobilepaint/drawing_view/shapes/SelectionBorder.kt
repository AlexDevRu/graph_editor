package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import android.util.Log
import com.example.mobilepaint.SelectionBorderOptions
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

class SelectionBorder(
    private val selectionBorderOptions : SelectionBorderOptions,
    private val listener: Listener
): Path() {

    interface Listener {
        fun onTransform(matrix: Matrix) = Unit
        /*fun onScale(matrix: Matrix) = Unit
        fun onRotate(matrix: Matrix) = Unit*/
    }

    private val bounds = RectF()

    private val handlePoints = FloatArray(8)
    private val matrix = Matrix()

    private var tlPointMoving = false
    private var trPointMoving = false
    private var blPointMoving = false
    private var brPointMoving = false

    private var isTranslating = false
    private var startX = 0f
    private var startY = 0f

    private var rotation = 0f

    private var sx = 1f
    private var sy = 1f

    private var centerX = 0f
    private var centerY = 0f
    private var startR = 0f
    private var startScale = 0f
    private var startRotation = 0f
    private var startA = 0f

    private val handleRadius get() = selectionBorderOptions.handleRadius
    private val handlePaint get() = selectionBorderOptions.handlePaint
    private val boundingBoxPaint get() = selectionBorderOptions.boundingBoxPaint

    fun drawInCanvas(canvas: Canvas) {
        canvas.drawPath(this, boundingBoxPaint)
        canvas.drawCircle(handlePoints[0], handlePoints[1], handleRadius, handlePaint)
        canvas.drawCircle(handlePoints[2], handlePoints[3], handleRadius, handlePaint)
        canvas.drawCircle(handlePoints[4], handlePoints[5], handleRadius, handlePaint)
        canvas.drawCircle(handlePoints[6], handlePoints[7], handleRadius, handlePaint)
    }

    private fun scale(sx: Float, sy: Float, px: Float, py: Float) {
        matrix.reset()
        matrix.setScale(sx, sy, px, py)
        transform(matrix)
        matrix.mapPoints(handlePoints)
        listener.onTransform(matrix)
    }

    private fun rotate(degrees: Float) {
        matrix.reset()
        matrix.setRotate(degrees, bounds.centerX(), bounds.centerY())
        transform(matrix)
        matrix.mapPoints(handlePoints)
        listener.onTransform(matrix)
    }

    fun down(x: Float, y: Float) {
        tlPointMoving = abs(x - handlePoints[0]) < handleRadius && abs(y - handlePoints[1]) < handleRadius
        trPointMoving = abs(x - handlePoints[2]) < handleRadius && abs(y - handlePoints[3]) < handleRadius
        blPointMoving = abs(x - handlePoints[6]) < handleRadius && abs(y - handlePoints[7]) < handleRadius
        brPointMoving = abs(x - handlePoints[4]) < handleRadius && abs(y - handlePoints[5]) < handleRadius
        isTranslating = !tlPointMoving && !trPointMoving && !blPointMoving && !brPointMoving && bounds.contains(x, y)

        if (brPointMoving || trPointMoving || tlPointMoving || blPointMoving) {
            val handleX = when {
                tlPointMoving -> handlePoints[0]
                trPointMoving -> handlePoints[2]
                blPointMoving -> handlePoints[6]
                else -> handlePoints[4]
            }

            val handleY = when {
                tlPointMoving -> handlePoints[1]
                trPointMoving -> handlePoints[3]
                blPointMoving -> handlePoints[7]
                else -> handlePoints[5]
            }

            centerX = (bounds.left + bounds.right) / 2f
            centerY = (bounds.top + bounds.bottom) / 2f
            startX = x - handleX + centerX
            startY = y - handleY + centerY
            startR = hypot(x - startX, y - startY)
            startA = Math.toDegrees(
                atan2(
                    y - startY,
                    x - startX
                ).toDouble()
            ).toFloat()
            startScale = sx
            startRotation = rotation
        } else {
            startX = x
            startY = y
        }
    }

    fun move(x: Float, y: Float) {
        if (brPointMoving || trPointMoving || tlPointMoving || blPointMoving) {
            val newR = hypot(x - startX, y - startY)
            val newA = Math.toDegrees(
                atan2(
                    y - startY,
                    x - startX
                ).toDouble()
            ).toFloat()
            val newScale = newR / startR * startScale
            val newRotation = newA - startA + startRotation

            scale(newScale / sx, newScale / sy, bounds.centerX(), bounds.centerY())
            rotate(newRotation - rotation)

            Log.d(TAG, "move: newRotation=$newRotation")

            sx = newScale
            sy = newScale
            rotation = newRotation
        } else if (isTranslating) {
            val dx = x - startX
            val dy = y - startY
            matrix.reset()
            matrix.setTranslate(dx, dy)
            matrix.mapPoints(handlePoints)
            transform(matrix)
            listener.onTransform(matrix)
            startX = x
            startY = y
        }
    }

    fun up(bounds : RectF) {
        this.bounds.set(bounds)
        if (isEmpty) {
            handlePoints[0] = bounds.left
            handlePoints[1] = bounds.top
            handlePoints[2] = bounds.right
            handlePoints[3] = bounds.top
            handlePoints[4] = bounds.right
            handlePoints[5] = bounds.bottom
            handlePoints[6] = bounds.left
            handlePoints[7] = bounds.bottom

            addRect(bounds, Direction.CW)
        }
    }

    companion object {
        private const val TAG = "SelectionBorder"
    }

}