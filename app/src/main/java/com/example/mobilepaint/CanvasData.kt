package com.example.mobilepaint

import com.example.mobilepaint.drawing_view.shapes.Shape
import java.util.*

data class CanvasData(
    val width: Int = 0,
    val height: Int = 0,
    val shapesList : LinkedList<Shape> = LinkedList(),
    val removedShapesList : LinkedList<Shape> = LinkedList()
)