package com.example.mobilepaint.models.json

class BitmapData(
    val shapeData: ShapeData,
    val base64 : String,
    val point : PointData,
    val matrix1 : FloatArray,
    val matrix2 : FloatArray,
    val sx : Float,
    val sy : Float,
    val rotation : Float,
)