package com.example.mobilepaint.models

data class MyImage(
    val id: String,
    val filePath: String,
    var canvasData: CanvasData,
    var published: Boolean
)
