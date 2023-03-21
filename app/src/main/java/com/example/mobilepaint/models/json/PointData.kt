package com.example.mobilepaint.models.json

import android.graphics.PointF

class PointData(
    val x : Float,
    val y : Float,
) {
    companion object {
        fun fromPoint(point: PointF) = PointData(point.x, point.y)
    }
}
