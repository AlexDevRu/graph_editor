package com.example.mobilepaint.drawing_view

import android.graphics.Matrix
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

class ScaleRotationTool(private val handleRadius: Float) {

    private val scaleMatrix = Matrix()
    private val rotateMatrix = Matrix()

    private var tlPointMoving = false
    private var trPointMoving = false
    private var blPointMoving = false
    private var brPointMoving = false

    private val handlePoints = FloatArray(8)

}