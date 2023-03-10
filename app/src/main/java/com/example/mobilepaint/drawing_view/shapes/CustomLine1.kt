package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import android.util.Log
import androidx.core.graphics.transform
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CustomLine1(override val paint: Paint) : RectF(), Shape {

    private val startPoint = PointF()
    private val endPoint = PointF()

    override fun down(x: Float, y: Float) {
        startPoint.x = x
        startPoint.y = y
        endPoint.x = x
        endPoint.y = y
    }

    override fun move(x: Float, y: Float) {
        endPoint.x = x
        endPoint.y = y
    }

    override fun up(x: Float, y: Float) {
        calculateCoordinates()
    }

    private fun calculateCoordinates() {
        val xMin = min(startPoint.x, endPoint.x)
        val xMax = max(startPoint.x, endPoint.x)
        val yMin = min(startPoint.y, endPoint.y)
        val yMax = max(startPoint.y, endPoint.y)
        left = xMin
        top = yMin
        right = xMax
        bottom = yMax
    }

    override fun drawInCanvas(canvas: Canvas) {
        canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, paint)
    }

    override fun applyShader(shader: Shader?) {
        paint.shader = shader
    }

    override fun changeColor(color: Int) {
        paint.color = color
    }

    override fun translate(dx: Float, dy: Float) {
        startPoint.x += dx
        endPoint.x += dx
        startPoint.y += dy
        endPoint.y += dy
    }

    override fun getBoundingBox() = this

    override fun isInside(x: Float, y: Float) = contains(x, y)

    override fun resize(dx: Float, dy: Float, handlePosition: Int) {
        //Log.d("line", "resize: ${height()} ${abs(startPoint.y - endPoint.y)}")
        //Log.d("line", "resize: ${height()}")
        Log.d("asd", "resize: $handlePosition")
        val dxx = endPoint.x - startPoint.x
        val yxx = endPoint.y - startPoint.y
        val tlToBr = dxx > 0 && yxx > 0
        val brToTl = dxx < 0 && yxx < 0
        val blToTr = dxx > 0 && yxx < 0
        val trToBl = dxx < 0 && yxx > 0
        when (handlePosition) {
            0 -> {
                if (tlToBr) {
                    startPoint.x += dx
                    startPoint.y += dy
                } else if (brToTl) {
                    endPoint.x += dx
                    endPoint.y += dy
                } else if (blToTr) {
                    startPoint.x += dx
                    endPoint.y += dy
                } else if (trToBl) {
                    endPoint.x += dx
                    startPoint.y += dy
                }
            }
            1 -> {
                if (tlToBr) {
                    endPoint.x += dx
                    startPoint.y += dy
                } else if (brToTl) {
                    startPoint.x += dx
                    endPoint.y += dy
                } else if (blToTr) {
                    endPoint.x += dx
                    endPoint.y += dy
                } else if (trToBl) {
                    startPoint.x += dx
                    startPoint.y += dy
                }
            }
            2 -> {
                left += dx
                bottom += dy
            }
            3 -> {
                if (tlToBr) {
                    endPoint.x += dx
                    endPoint.y += dy
                } else if (brToTl) {
                    startPoint.x += dx
                    startPoint.y += dy
                } else if (blToTr) {
                    endPoint.x += dx
                    startPoint.y += dy
                } else if (trToBl) {
                    startPoint.x += dx
                    endPoint.y += dy
                }
            }
        }

        /*val oldWidth = width()

        when (handlePosition) {
            0 -> {
                left += dx
                top += dy
            }
            1 -> {
                right += dx
                top += dy
            }
            2 -> {
                left += dx
                bottom += dy
            }
            3 -> {
                right += dx
                bottom += dy
            }
        }

        val newWidth = width()

        val sx = 1 + dx / width()
        val sy = 1 + dy / height()

        val scaleMatrix = Matrix()
        when (handlePosition) {
            0 -> {
                scaleMatrix.setScale(sx, sy, right, bottom)
            }
            1 -> {

            }
            2 -> {

            }
            3 -> {

            }
        }
        transform(scaleMatrix)*/
    }

}