package com.example.mobilepaint.drawing_view

import android.graphics.Matrix
import com.example.mobilepaint.drawing_view.shapes.Shape

sealed class Operation {
    abstract val shape: Shape

    data class Creation(override val shape: Shape) : Operation()
    data class PointMoving(override val shape: Shape, val isStartPoint: Boolean, val x : Float, val y : Float) : Operation()
    data class Transformation(override val shape: Shape, val matrix : Matrix) : Operation()
}
