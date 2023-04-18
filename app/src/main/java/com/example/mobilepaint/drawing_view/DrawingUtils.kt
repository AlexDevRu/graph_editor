package com.example.mobilepaint.drawing_view

import android.content.Context
import android.graphics.*
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.example.mobilepaint.R
import com.example.mobilepaint.Utils
import com.example.mobilepaint.Utils.toPx
import com.example.mobilepaint.drawing_view.shapes.*
import com.example.mobilepaint.models.CanvasData
import com.example.mobilepaint.models.SelectionBorderOptions
import com.example.mobilepaint.models.json.*
import com.google.gson.Gson

class DrawingUtils(private val context: Context) {

    private val selectionColors = intArrayOf(
        ContextCompat.getColor(context, R.color.red),
        ContextCompat.getColor(context, R.color.blue),
        ContextCompat.getColor(context, R.color.green),
        ContextCompat.getColor(context, R.color.yellow),
    )

    private val editTextPaint = EditText(context).paint

    private val gson = Gson()

    private val handleRadius = 8.toPx
    val arrowWidth = context.resources.getDimension(R.dimen.drawing_view_arrow_width)
    val arrowHeight = arrowWidth * 1.2f

    val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    val shader =
        LinearGradient(0f, 0f, 100f, 20f, selectionColors, null, Shader.TileMode.MIRROR)

    private val boundingBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.toPx
        color = Color.BLACK
        pathEffect = DashPathEffect(floatArrayOf(20.toPx, 5.toPx), 0f)
    }

    val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    fun createPaint(strokeWidthDp: Float = 5f, color: Int = Color.BLACK) = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = strokeWidthDp.toPx
        it.color = color
    }

    fun getSelectionBorderOptions() = SelectionBorderOptions(
        handlePaint, handleRadius, boundingBoxPaint
    )

    fun createPathPaint(strokeWidthDp: Float = 5f, color: Int = Color.BLACK) = Paint(createPaint(strokeWidthDp, color)).apply {
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    fun fromJson(json : String) : CanvasData {
        val canvasJson = gson.fromJson(json, CanvasJson::class.java)
        val shapes = canvasJson.shapesList.map {
            when (it.type) {
                GeometryType.LINE.name -> {
                    val lineData = gson.fromJson(it.data, LineData::class.java)
                    CustomLine(handlePaint, shader, createPaint()).apply {
                        addData(lineData)
                    }
                }
                GeometryType.PATH.name -> {
                    val pathData = gson.fromJson(it.data, PathData::class.java)
                    CustomPath(getSelectionBorderOptions(), shader, createPathPaint()).apply {
                        addData(pathData)
                    }
                }
                GeometryType.RECT.name -> {
                    val rectData = gson.fromJson(it.data, RectData::class.java)
                    CustomRectF(getSelectionBorderOptions(), shader, createPaint()).apply {
                        addData(rectData)
                    }
                }
                GeometryType.ELLIPSE.name -> {
                    val rectData = gson.fromJson(it.data, RectData::class.java)
                    CustomEllipse(getSelectionBorderOptions(), shader, createPaint()).apply {
                        addData(rectData)
                    }
                }
                GeometryType.ARROW.name -> {
                    val lineData = gson.fromJson(it.data, LineData::class.java)
                    CustomArrow(arrowWidth, arrowHeight, handlePaint, shader, createPaint()).apply {
                        addData(lineData)
                    }
                }
                GeometryType.BITMAP.name -> {
                    val bitmapData = gson.fromJson(it.data, BitmapData::class.java)
                    val bitmap = Utils.convert(bitmapData.base64)
                    CustomBitmap(bitmap, getSelectionBorderOptions(), bitmapPaint).apply {
                        addData(bitmapData)
                    }
                }
                GeometryType.TEXT.name -> {
                    val textData = gson.fromJson(it.data, TextData::class.java)
                    CustomText(editTextPaint, textData)
                }
                else -> throw IllegalStateException()
            }
        }

        return CanvasData(
            title = canvasJson.title,
            width = canvasJson.width,
            height = canvasJson.height,
            bg = canvasJson.bg,
            shapesList = shapes
        )
    }

    fun getBitmap(canvasData: CanvasData) : Bitmap {
        val drawingView = DrawingView(context)
        drawingView.measure(
            View.MeasureSpec.makeMeasureSpec(canvasData.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(canvasData.height, View.MeasureSpec.EXACTLY),
        )
        drawingView.layout(0, 0, canvasData.width, canvasData.height)
        drawingView.addShapes(canvasData.shapesList, canvasData.removedShapesList)
        return drawingView.getBitmap()
    }
}