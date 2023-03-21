package com.example.mobilepaint.models.json

class CanvasJson(
    val width: Int,
    val height: Int,
    val shapesList : List<ShapeJson>
)

data class ShapeJson(
    val type: String,
    val data : String
)