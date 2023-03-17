package com.example.mobilepaint.drawing_view.shapes

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import com.example.mobilepaint.drawing_view.Operation

interface Shape {
    val paint: Paint

    fun drawInCanvas(canvas: Canvas)
    fun down(x: Float, y: Float)
    fun move(x: Float, y: Float)
    fun up(x: Float, y: Float) : Operation? = null
    fun isInside(x: Float, y: Float) : Boolean
    fun changeColor(@ColorInt color: Int)
    fun fillColor(@ColorInt color: Int) = false
    fun setSelected(selected: Boolean)
    fun applyOperation(operation: Operation) : Operation? = null
}
