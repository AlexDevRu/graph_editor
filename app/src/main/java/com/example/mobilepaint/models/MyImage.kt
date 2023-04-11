package com.example.mobilepaint.models

data class MyImage(
    var canvasData: CanvasData,
    val title: String,
    var published: Boolean
)
