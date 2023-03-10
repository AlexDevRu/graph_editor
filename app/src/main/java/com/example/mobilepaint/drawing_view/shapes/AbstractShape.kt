package com.example.mobilepaint.drawing_view.shapes

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.annotation.ColorInt

abstract class AbstractShape : RectF(), Shape {
    /*abstract val paint: Paint

    abstract fun drawInCanvas(canvas: Canvas)
    abstract fun down(x: Float, y: Float)
    abstract fun move(x: Float, y: Float)
    abstract fun up(x: Float, y: Float)

    abstract fun translate(dx: Float, dy: Float)

    abstract fun applyShader(shader: Shader?)
    abstract override fun changeColor(@ColorInt color: Int)
    override fun fillColor(@ColorInt color: Int) = false
    abstract override fun resize(dx: Float, dy: Float, handlePosition: Int)

    override fun getBoundingBox(): RectF {
        TODO("Not yet implemented")
    }

    override fun isInside(x: Float, y: Float): Boolean {
        TODO("Not yet implemented")
    }*/
}