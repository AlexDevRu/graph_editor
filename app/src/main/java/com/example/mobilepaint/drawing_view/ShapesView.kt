package com.example.mobilepaint.drawing_view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.example.mobilepaint.R
import com.example.mobilepaint.Utils
import com.example.mobilepaint.Utils.toPx
import com.example.mobilepaint.drawing_view.shapes.*
import com.example.mobilepaint.models.CanvasData
import com.example.mobilepaint.models.SelectionBorderOptions
import com.example.mobilepaint.models.json.*
import com.google.gson.Gson
import java.util.*


class ShapesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    companion object {
        private const val IMAGE_SIZE = 300
        private const val TAG = "ShapesView"
    }

    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }

    private val arrowWidth = resources.getDimension(R.dimen.drawing_view_arrow_width)
    private val arrowHeight = arrowWidth * 1.2f

    private val handleRadius = 8.toPx

    val shapes = mutableListOf<Shape>()
    val removedShapes = mutableListOf<Shape>()

    private val operations = LinkedList<Operation>()
    private val removedOperations = LinkedList<Operation>()

    private var currentShape: Shape? = null

    private var isBorderDrawing = true

    private fun getSelectionBorderOptions() = SelectionBorderOptions(
        handlePaint, handleRadius, boundingBoxPaint
    )

    interface OnShapeChanged {
        fun onStackSizesChanged(addedShapesSize: Int, removedShapesSize: Int)
        fun onShapeLongClick(shape: Shape)
    }

    private var onShapeChanged: OnShapeChanged? = null

    val hasModifications get() = shapes.isNotEmpty()

    fun addNewShape(shape: Shape) {
        shapes.add(shape)
        onShapeChanged?.onStackSizesChanged(shapes.size, removedShapes.size)
    }

    fun addShapes(shapes: List<Shape>, removedShapes : List<Shape>) {
        this.shapes.clear()
        this.removedShapes.clear()
        this.shapes.addAll(shapes)
        this.removedShapes.addAll(removedShapes)
        invalidate()
    }

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 4.toPx
        pathEffect = DashPathEffect(floatArrayOf(50.toPx, 10.toPx, 5.toPx, 10.toPx), 25f)
    }

    fun addBitmap(bitmap: Bitmap) {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE,true)
        val customBitmap = CustomBitmap(scaledBitmap, getSelectionBorderOptions(), bitmapPaint)
        addNewShape(customBitmap)
    }

    fun removeShape(shape: Shape) {
        if (selectedShape == shape)
            deselectShape()
        shapes.remove(shape)
        onShapeChanged?.onStackSizesChanged(shapes.size, removedShapes.size)
        invalidate()
    }

    @ColorInt
    var color = Color.BLACK
        set(value) {
            field = value
            selectedShape?.changeColor(field)
        }

    @ColorInt
    var canvasColor = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    var strokeWidth = 1f

    var geometryType: GeometryType = GeometryType.PATH
        set(value) {
            field = value
            if (field != GeometryType.HAND)
                deselectShape()
        }

    var selectedShape: Shape? = null
        private set

    private val selectionColors = intArrayOf(
        ContextCompat.getColor(context, R.color.red),
        ContextCompat.getColor(context, R.color.blue),
        ContextCompat.getColor(context, R.color.green),
        ContextCompat.getColor(context, R.color.yellow),
    )

    private val handler = Handler(Looper.getMainLooper())
    private val onLongPressed = Runnable {
        selectedShape?.let {
            onShapeChanged?.onShapeLongClick(it)
        }
    }

    private val shader =
        LinearGradient(0f, 0f, 100f, 20f, selectionColors, null, Shader.TileMode.MIRROR)

    fun undo() {
        if (operations.isNotEmpty()) {
            when(val operation = operations.pop()) {
                is Operation.Creation -> {
                    shapes.remove(operation.shape)
                    removedShapes.add(operation.shape)
                    removedOperations.push(operation)
                }
                else -> {
                    val invertOperation = operation.shape.applyOperation(operation)
                    if (invertOperation != null)
                        removedOperations.push(invertOperation)
                }
            }
            Log.e(TAG, "onTouchEvent: operation undo ${operations.size} operations=$operations")
            Log.e(TAG, "onTouchEvent: operation undo ${removedOperations.size} removedOperations=$removedOperations")
            onShapeChanged?.onStackSizesChanged(operations.size, removedOperations.size)
            invalidate()
        }
    }

    fun redo() {
        if (removedOperations.isNotEmpty()) {
            when(val operation = removedOperations.pop()) {
                is Operation.Creation -> {
                    removedShapes.remove(operation.shape)
                    shapes.add(operation.shape)
                    operations.push(operation)
                }
                else -> {
                    val invertOperation = operation.shape.applyOperation(operation)
                    if (invertOperation != null)
                        operations.push(invertOperation)
                }
            }
            onShapeChanged?.onStackSizesChanged(operations.size, removedOperations.size)
            invalidate()
        }
    }

    fun setOnShapeChangedListener(onShapeChanged: OnShapeChanged?) {
        this.onShapeChanged = onShapeChanged
    }

    private val boundingBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.toPx
        color = Color.BLACK
        pathEffect = DashPathEffect(floatArrayOf(20.toPx, 5.toPx), 0f)
    }

    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    private fun createPaint() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@ShapesView.strokeWidth.toPx
        color = this@ShapesView.color
    }

    private fun createPathPaint() = Paint(createPaint()).apply {
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null || geometryType == GeometryType.TEXT)
            return false

        val touchX = event.x
        val touchY = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (geometryType == GeometryType.PAINT) {
                    val shape = shapes.firstOrNull { it.isInside(touchX, touchY) }
                    if (shape != null)
                        shape.fillColor(color)
                    else
                        canvasColor = color
                } else if (geometryType == GeometryType.HAND) {
                    handler.postDelayed(onLongPressed, 1000)
                    selectedShape?.down(touchX, touchY)
                } else {
                    currentShape = when (geometryType) {
                        GeometryType.PATH -> CustomPath(getSelectionBorderOptions(), shader, createPathPaint())
                        GeometryType.LINE -> CustomLine(handlePaint, shader, createPaint())
                        GeometryType.RECT -> CustomRectF(getSelectionBorderOptions(), shader, createPaint())
                        GeometryType.ELLIPSE -> CustomEllipse(getSelectionBorderOptions(), shader, createPaint())
                        GeometryType.ARROW -> CustomArrow(arrowWidth, arrowHeight, handlePaint, shader, createPaint())
                        else -> null
                    }
                    currentShape?.down(touchX, touchY)
                    if (currentShape != null)
                        addNewShape(currentShape!!)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                handler.removeCallbacks(onLongPressed)
                selectedShape?.move(touchX, touchY)
                currentShape?.move(touchX, touchY)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(onLongPressed)
                if (geometryType == GeometryType.HAND) {
                    val operation = selectedShape?.up(touchX, touchY)
                    if (operation != null) {
                        operations.push(operation)
                        Log.e(TAG, "onTouchEvent: operation added $operations")
                    }
                    deselectShape()
                    selectedShape = shapes.firstOrNull { it.isInside(touchX, touchY) }
                    selectedShape?.setSelected(true)
                    if (selectedShape is CustomText) {
                        val text = selectedShape as CustomText
                        onTextSelected?.invoke(text)
                    }
                } else {
                    val operation = currentShape?.up(touchX, touchY)
                    if (operation != null) {
                        operations.push(operation)
                        Log.e(TAG, "onTouchEvent: operation added $operations")
                    }
                    currentShape = null
                }
            }
            else -> return false
        }

        invalidate()
        return true
    }

    var onTextSelected: ((CustomText) -> Unit)? = null

    fun deselectShape() {
        selectedShape?.setSelected(false)
        selectedShape = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(canvasColor)
        if (isBorderDrawing)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), borderPaint)
        for (shape in shapes)
            shape.drawInCanvas(canvas)
    }

    fun getBitmap(): Bitmap {
        deselectShape()
        isBorderDrawing = false
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        isBorderDrawing = true
        return bitmap
    }

    fun fromJson(editTextPaint: Paint, json : String, gson: Gson) : CanvasData {
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
            width = canvasJson.width,
            height = canvasJson.height,
            bg = canvasJson.bg,
            shapesList = shapes
        )
    }
}