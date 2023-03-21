package com.example.mobilepaint.models.json

class PathData(
    val shapeData: ShapeData,
    val points: List<PointData>,
    val matrix : FloatArray
)
