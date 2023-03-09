package com.example.mobilepaint.drawing_view.shapes

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader

interface Shape {
    val paint: Paint

    fun drawInCanvas(canvas: Canvas)
    fun down(x: Float, y: Float)
    fun move(x: Float, y: Float)
    fun up() = Unit
    fun isInside(x: Float, y: Float) : Boolean
    fun getBoundingBox() : RectF = RectF()

    fun translate(dx: Float, dy: Float) = Unit

    fun applyShader(shader: Shader?)
}
