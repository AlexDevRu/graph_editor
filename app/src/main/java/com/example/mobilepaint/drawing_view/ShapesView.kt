package com.example.mobilepaint.drawing_view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.example.mobilepaint.R
import com.example.mobilepaint.Utils.toPx
import com.example.mobilepaint.drawing_view.shapes.*
import java.util.*


class ShapesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }

    private val arrowWidth = resources.getDimension(R.dimen.drawing_view_arrow_width)
    private val arrowHeight = arrowWidth * 1.2f

    private val shapes = LinkedList<Shape>()
    private val removedShapes = LinkedList<Shape>()

    fun getShapesList() : List<Shape> = shapes
    fun getRemovedShapesList() : List<Shape> = removedShapes

    private var currentShape: Shape? = null

    interface OnShapeChanged {
        fun onStackSizesChanged(addedShapesSize: Int, removedShapesSize: Int)
        fun onShapeLongClick(shape: Shape)
    }

    private var onShapeChanged: OnShapeChanged? = null

    val hasModifications get() = shapes.isNotEmpty()

    fun addNewShape(shape: Shape) {
        shapes.push(shape)
        onShapeChanged?.onStackSizesChanged(shapes.size, removedShapes.size)
    }

    fun addShapes(shapes: LinkedList<Shape>, removedShapes : LinkedList<Shape>) {
        this.shapes.clear()
        this.removedShapes.clear()
        this.shapes.addAll(shapes)
        this.removedShapes.addAll(removedShapes)
        invalidate()
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

    private var selectedShape: Shape? = null

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
        if (shapes.isNotEmpty()) {
            val shape = shapes.pop()
            if (shape == selectedShape)
                deselectShape()
            removedShapes.push(shape)
            onShapeChanged?.onStackSizesChanged(shapes.size, removedShapes.size)
            invalidate()
        }
    }

    fun redo() {
        if (removedShapes.isNotEmpty()) {
            val shape = removedShapes.pop()
            shapes.push(shape)
            onShapeChanged?.onStackSizesChanged(shapes.size, removedShapes.size)
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
                    val shape = shapes.lastOrNull { it.isInside(touchX, touchY) }
                    if (shape != null)
                        shape.fillColor(color)
                    else
                        canvasColor = color
                } else if (geometryType == GeometryType.HAND) {
                    handler.postDelayed(onLongPressed, 1000)
                } else {
                    currentShape = when (geometryType) {
                        GeometryType.PATH -> CustomPath(handlePaint, boundingBoxPaint, shader, createPathPaint())
                        GeometryType.LINE -> CustomLine(handlePaint, shader, createPaint())
                        GeometryType.RECT -> CustomRectF(handlePaint, boundingBoxPaint, shader, createPaint())
                        GeometryType.ELLIPSE -> CustomEllipse(handlePaint, boundingBoxPaint, shader, createPaint())
                        GeometryType.ARROW -> CustomArrow(arrowWidth, arrowHeight, handlePaint, shader, createPaint())
                        else -> null
                    }
                    selectedShape?.down(touchX, touchY)
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
                    selectedShape?.up(touchX, touchY)
                    deselectShape()
                    selectedShape = shapes.lastOrNull { it.isInside(touchX, touchY) }
                    selectedShape?.setSelected(true)
                } else {
                    currentShape?.up(touchX, touchY)
                    currentShape = null
                }
            }
            else -> return false
        }

        invalidate()
        return true
    }

    private fun deselectShape() {
        selectedShape?.setSelected(false)
        selectedShape = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(canvasColor)
        for (shape in shapes.descendingIterator())
            shape.drawInCanvas(canvas)
    }

    fun getBitmap(): Bitmap {
        deselectShape()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }
}