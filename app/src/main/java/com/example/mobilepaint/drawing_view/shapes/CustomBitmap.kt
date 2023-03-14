package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import android.util.Log
import kotlin.math.min

class CustomBitmap(
    private val bitmap: Bitmap,
    handlePaint: Paint,
    boundingBoxPaint: Paint,
    selectionShader: Shader?,
    paint: Paint
): CustomRectF(handlePaint, boundingBoxPaint, selectionShader, paint) {

    private var imageSize = 300

    private var scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize,true)

    private val matrix = Matrix()

    init {
        left = 0f
        top = 0f
        right = imageSize.toFloat()
        bottom = imageSize.toFloat()
    }

    override fun move(x: Float, y: Float) {
        super.move(x, y)
        if (width() != imageSize.toFloat() || height() != imageSize.toFloat()) {
            imageSize = min(width().toInt(), height().toInt())

            val point = when {
                tlPointMoving -> PointF(right, bottom)
                trPointMoving -> PointF(left, bottom)
                blPointMoving -> PointF(right, top)
                brPointMoving -> PointF(left, top)
                else -> return
            }

            val sx = imageSize.toFloat() / bitmap.width
            val sy = imageSize.toFloat() / bitmap.height
            Log.d(TAG, "move: sx=$sx")
            Log.d(TAG, "move: sy=$sy")
            matrix.reset()
            matrix.setScale(sx, sy, point.x, point.y)
            scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
    }

    override fun drawInCanvas(canvas: Canvas) {
        canvas.drawBitmap(scaledBitmap, left, top, paint)
        if (selected1) {
            canvas.drawRect(this, boundingBoxPaint)
            canvas.drawCircle(left, top, handleRadius, handlePaint)
            canvas.drawCircle(right, top, handleRadius, handlePaint)
            canvas.drawCircle(left, bottom, handleRadius, handlePaint)
            canvas.drawCircle(right, bottom, handleRadius, handlePaint)
        }
    }

    companion object {
        private const val TAG = "CustomBitmap"
    }

}
