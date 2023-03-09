package com.example.mobilepaint

import androidx.annotation.DrawableRes
import com.example.mobilepaint.drawing_view.GeometryType

data class PenType(
    val text: String,
    @DrawableRes val iconRes: Int,
    val geometryType: GeometryType
) {
    override fun toString(): String {
        return text
    }
}
