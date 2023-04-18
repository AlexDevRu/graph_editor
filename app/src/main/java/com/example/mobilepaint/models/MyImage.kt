package com.example.mobilepaint.models

data class MyImage(
    val id: String,
    var canvasData: CanvasData,
    var published: Boolean
)
