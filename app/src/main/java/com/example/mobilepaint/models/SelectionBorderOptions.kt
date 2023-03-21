package com.example.mobilepaint.models

import android.graphics.Paint

data class SelectionBorderOptions(
    val handlePaint : Paint,
    val handleRadius: Float,
    val boundingBoxPaint : Paint,
)
