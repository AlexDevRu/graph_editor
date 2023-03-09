package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import kotlin.math.abs
import kotlin.math.atan

class CustomArrow(
    private val arrowWidth: Float,
    private val arrowHeight: Float,
    override val paint: Paint
): RectF(), Shape {

    companion object {
        private const val PI = Math.PI.toFloat()
    }

    private val triangle = Path()

    private val matrix = Matrix()
    private val triangleBounds = RectF()
    private val bounds = RectF()

    private val trianglePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = paint.color
    }

    override fun drawInCanvas(canvas: Canvas) {
        canvas.drawLine(left, top, right, bottom, paint)
        canvas.drawPath(triangle, trianglePaint)
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

        val arrowWidthHalf = arrowWidth / 2

        triangle.reset()
        triangle.moveTo(x, y)
        triangle.lineTo(x - arrowWidthHalf, y)
        triangle.lineTo(x, y - arrowHeight)
        triangle.lineTo(x + arrowWidthHalf, y)
        triangle.lineTo(x, y)

        val k1 = abs(left - x)
        val k2 = abs(top - y)

        val angleX = atan(k2 / k1) * 180 / PI
        val angle = when {
            // I
            x > left && y < top -> 90 - angleX
            // III
            x < left && y > top -> 270 - angleX
            // II
            x < left && y < top -> -90 + angleX
            // IV
            x > left && y > top -> 90 + angleX
            else -> 0f
        }

        triangle.computeBounds(triangleBounds, true)
        matrix.reset()
        matrix.postRotate(angle, triangleBounds.centerX(), triangleBounds.bottom)
        triangle.transform(matrix)
    }

    override fun up() {
        triangle.computeBounds(triangleBounds, true)
        bounds.left = minOf(left, right, triangleBounds.left, triangleBounds.right)
        bounds.right = maxOf(left, right, triangleBounds.left, triangleBounds.right)
        bounds.top = minOf(top, bottom, triangleBounds.top, triangleBounds.bottom)
        bounds.bottom = maxOf(top, bottom, triangleBounds.top, triangleBounds.bottom)
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
        triangle.offset(dx, dy)
    }

    override fun applyShader(shader: Shader?) {
        paint.shader = shader
        trianglePaint.shader = shader
    }

    override fun changeColor(color: Int) {
        paint.color = color
        trianglePaint.color = color
    }
}