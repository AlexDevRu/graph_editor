package com.example.mobilepaint.models

import com.example.mobilepaint.drawing_view.GeometryType
import com.example.mobilepaint.drawing_view.shapes.*
import com.example.mobilepaint.models.json.CanvasJson
import com.example.mobilepaint.models.json.ShapeJson
import com.google.gson.Gson
import java.util.*

data class CanvasData(
    val width: Int = 0,
    val height: Int = 0,
    val shapesList : List<Shape> = LinkedList(),
    val removedShapesList : List<Shape> = LinkedList()
) {
    private fun getType(shape: Shape) = when (shape) {
        is CustomLine -> GeometryType.LINE
        is CustomArrow -> GeometryType.ARROW
        is CustomRectF -> GeometryType.RECT
        is CustomEllipse -> GeometryType.ELLIPSE
        is CustomPath -> GeometryType.PATH
        is CustomBitmap -> GeometryType.BITMAP
        else -> throw IllegalStateException()
    }

    fun toJson(gson: Gson) : String {
        val jsonShapes = shapesList.map { ShapeJson(getType(it).name, it.toJson(gson)) }
        val canvasJson = CanvasJson(
            width = width,
            height = height,
            shapesList = jsonShapes,
        )
        return gson.toJson(canvasJson)
    }
}