package com.example.mobilepaint.drawing_view.shapes

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.annotation.ColorInt

interface Shape {
    val paint: Paint

    fun drawInCanvas(canvas: Canvas)
    fun down(x: Float, y: Float)
    fun move(x: Float, y: Float)
    fun up() = Unit
    fun isInside(x: Float, y: Float) : Boolean
    fun getBoundingBox() : RectF

    fun translate(dx: Float, dy: Float)

    fun applyShader(shader: Shader?)
    fun changeColor(@ColorInt color: Int)
    fun fillColor(@ColorInt color: Int) = false
}
