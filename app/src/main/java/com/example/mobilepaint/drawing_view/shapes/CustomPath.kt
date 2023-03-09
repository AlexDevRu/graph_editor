package com.example.mobilepaint.drawing_view.shapes

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

class CustomPath(
    override val paint: Paint
): Path(), Shape {

    private val bounds = RectF()

    override fun drawInCanvas(canvas: Canvas) {
        canvas.drawPath(this, paint)
    }

    override fun down(x: Float, y: Float) {
        moveTo(x, y)
    }

    override fun move(x: Float, y: Float) {
        lineTo(x, y)
    }

    override fun up() {
        computeBounds(bounds, true)
    }

    override fun isInside(x: Float, y: Float): Boolean {
        return bounds.contains(x, y)
    }

    override fun getBoundingBox(): RectF {
        return bounds
    }
}