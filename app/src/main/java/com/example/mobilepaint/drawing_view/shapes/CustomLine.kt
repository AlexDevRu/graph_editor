package com.example.mobilepaint.drawing_view.shapes

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.max
import kotlin.math.min

class CustomLine(
    override val paint: Paint
): RectF(), Shape {

    private val bounds = RectF()

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

    override fun up() {
        bounds.top = min(top, bottom)
        bounds.bottom = max(top, bottom)
        bounds.left = min(left, right)
        bounds.right = max(left, right)
    }

    override fun drawInCanvas(canvas: Canvas) {
        canvas.drawLine(left, top, right, bottom, paint)
    }

    override fun isInside(x: Float, y: Float): Boolean {
        return bounds.contains(x, y)
    }

    override fun getBoundingBox(): RectF {
        return bounds
    }

    override fun translate(dx: Float, dy: Float) {
        left += dx
        right += dx
        top += dy
        bottom += dy
    }

    override fun applyShader(shader: Shader?) {
        paint.shader = shader
    }

}