package com.example.mobilepaint.drawing_view.shapes

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import androidx.annotation.ColorInt

interface Shape {
    val paint: Paint

    fun drawInCanvas(canvas: Canvas)
    fun down(x: Float, y: Float)
    fun move(x: Float, y: Float)
    fun up(x: Float, y: Float) = Unit
    fun isInside(x: Float, y: Float) : Boolean
    fun changeColor(@ColorInt color: Int)
    fun fillColor(@ColorInt color: Int) = false
    fun setSelected(selected: Boolean)
}
