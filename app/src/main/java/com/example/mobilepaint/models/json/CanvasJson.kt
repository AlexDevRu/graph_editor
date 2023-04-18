package com.example.mobilepaint.models.json

data class CanvasJson(
    val title: String,
    val width: Int,
    val height: Int,
    val bg: Int,
    val shapesList : List<ShapeJson>
)

data class ShapeJson(
    val type: String,
    val data : String
)