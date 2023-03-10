package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import android.util.Log

class CustomPath(
    override val paint: Paint
): Path(), Shape {

    private val bounds = RectF()

    private var path1 = Path()

    override fun drawInCanvas(canvas: Canvas) {
        canvas.drawPath(this, paint)
    }

    override fun down(x: Float, y: Float) {
        moveTo(x, y)
    }

    override fun move(x: Float, y: Float) {
        lineTo(x, y)
    }

    override fun up(x: Float, y: Float) {
        computeBounds(bounds, true)
        initialBounds.set(bounds)
        path1 = Path(this)
        dx = 0f
        dy = 0f
        /*val scaleMatrix = Matrix()
        scaleMatrix.setScale(1 / sx, 1 / sy, px, py)
        transform(scaleMatrix)
        sx = 1f
        sy = 1f*/
        /*sx = 1f
        sy = 1f*/
        initResize = true
    }

    override fun isInside(x: Float, y: Float): Boolean {
        return bounds.contains(x, y)
    }

    override fun getBoundingBox(): RectF {
        return bounds
    }

    override fun translate(dx: Float, dy: Float) {
        offset(dx, dy)
    }

    override fun applyShader(shader: Shader?) {
        paint.shader = shader
    }

    override fun changeColor(color: Int) {
        paint.color = color
    }

    //private val scaleMatrix = Matrix()

    //private var newBounds = RectF()

    private val initialBounds = RectF()

    private var dx = 0f
    private var dy = 0f
    private var sx = 1f
    private var sy = 1f

    var px = bounds.left
    var py = bounds.top

    private var initResize = true

    private val scaleMatrix = Matrix()

    fun resize1(oldWidth: Float, oldHeight: Float, newWidth: Float, newHeight: Float, handlePosition: Int) {
        px = bounds.left
        py = bounds.top

        when (handlePosition) {
            0 -> {
                px = bounds.right
                py = bounds.bottom
            }
            1 -> {
                px = bounds.left
                py = bounds.bottom
            }
            2 -> {
                px = bounds.right
                py = bounds.top
            }
            3 -> {
                px = bounds.left
                py = bounds.top
            }
        }

        val sx = newWidth / oldWidth
        val sy = newHeight / oldHeight

        Log.d("asd", "resize: old scaleY=${this.sx}")
        Log.d("asd", "resize: old scaleX=${this.sy}")
        Log.d("asd", "resize: scaleX=${sx}")
        Log.d("asd", "resize: scaleX=${sy}")

        val invert = Matrix()
        scaleMatrix.invert(invert)
        transform(invert)
        scaleMatrix.setScale(sx, sy, px, py)
        transform(scaleMatrix)

        this.sx = sx
        this.sy = sy

        computeBounds(bounds, true)

        initResize = false
    }

    override fun resize(dx: Float, dy: Float, handlePosition: Int) {
        /*this.dx += dx
        this.dy += dy

        val sx = 1 + this.dx / initialBounds.width()

        val scaleMatrix = Matrix()
        scaleMatrix.setScale(scaleX, scaleY, px, py)
        transform(scaleMatrix)*/

        /*val newBounds = RectF(bounds)

        when (handlePosition) {
            0 -> {
                newBounds.left += dx
                newBounds.top += dy
            }
            1 -> {
                newBounds.right += dx
                newBounds.top += dy
            }
            2 -> {
                newBounds.left += dx
                newBounds.bottom += dy
            }
            3 -> {
                newBounds.right += dx
                newBounds.bottom += dy
            }
        }

        Log.d("asd", "resize: bounds=${bounds.left} - ${bounds.top} ; ${bounds.right} - ${bounds.bottom}")
        Log.d("asd", "resize: newBounds=${newBounds.left} - ${newBounds.top} ; ${newBounds.right} - ${newBounds.bottom}")
        Log.d("asd", "resize: bounds width=${bounds.width()}")
        Log.d("asd", "resize: bounds height=${bounds.height()}")
        Log.d("asd", "resize: newBounds width=${newBounds.width()}")
        Log.d("asd", "resize: newBounds height=${newBounds.height()}")
        sy = newBounds.height() / bounds.height()
        sx = newBounds.width() / bounds.width()
        Log.d("asd", "resize: scaleY=${sy}")
        Log.d("asd", "resize: scaleX=${sx}")
        Log.d("asd", "===============================")

        var px = bounds.left
        var py = bounds.top

        val hh = if (handlePosition == 0 && newBounds.width() < 0) 1 else handlePosition

        when (hh) {
            0 -> {
                px = bounds.right
                py = bounds.bottom
            }
            1 -> {
                px = bounds.left
                py = bounds.bottom
            }
            2 -> {
                px = bounds.right
                py = bounds.top
            }
            3 -> {
                px = bounds.left
                py = bounds.top
            }
        }

        val scaleMatrix = Matrix()
        scaleMatrix.setScale(sx, sy, px, py)
        transform(scaleMatrix)

        computeBounds(bounds, true)*/

        /*val newBounds = RectF(bounds)

        when (handlePosition) {
            0 -> {
                newBounds.left += dx
                newBounds.top += dy
            }
            1 -> {
                newBounds.right += dx
                newBounds.top += dy
            }
            2 -> {
                newBounds.left += dx
                newBounds.bottom += dy
            }
            3 -> {
                newBounds.right += dx
                newBounds.bottom += dy
            }
        }

        Log.d("asd", "resize: bounds=${bounds.left} - ${bounds.top} ; ${bounds.right} - ${bounds.bottom}")
        Log.d("asd", "resize: newBounds=${newBounds.left} - ${newBounds.top} ; ${newBounds.right} - ${newBounds.bottom}")
        Log.d("asd", "resize: bounds width=${bounds.width()}")
        Log.d("asd", "resize: bounds height=${bounds.height()}")
        Log.d("asd", "resize: newBounds width=${newBounds.width()}")
        Log.d("asd", "resize: newBounds height=${newBounds.height()}")
        val scaleY = newBounds.height() / bounds.height()
        val scaleX = newBounds.width() / bounds.width()
        Log.d("asd", "resize: scaleY=${scaleY}")
        Log.d("asd", "resize: scaleX=${scaleX}")
        Log.d("asd", "===============================")

        var px = bounds.left
        var py = bounds.top

        when (handlePosition) {
            0 -> {
                px = bounds.right
                py = bounds.bottom
            }
            1 -> {
                px = bounds.left
                py = bounds.bottom
            }
            2 -> {
                px = bounds.right
                py = bounds.top
            }
            3 -> {
                px = bounds.left
                py = bounds.top
            }
        }

        val scaleMatrix = Matrix()
        scaleMatrix.setScale(scaleX, scaleY, px, py)
        transform(scaleMatrix)

        computeBounds(bounds, true)*/
    }
}