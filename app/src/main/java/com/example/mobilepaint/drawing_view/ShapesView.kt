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

    var strokeWidth = 1f

    var geometryType: GeometryType = GeometryType.PATH
        set(value) {
            field = value
            if (field != GeometryType.HAND)
                deselectShape()
        }

    private var selectedShape: Shape? = null
    private var selection: CustomRectF? = null

    private val selectionColors = intArrayOf(
        ContextCompat.getColor(context, R.color.red),
        ContextCompat.getColor(context, R.color.blue),
        ContextCompat.getColor(context, R.color.green),
        ContextCompat.getColor(context, R.color.yellow),
    )

    private var startX = 0f
    private var startY = 0f
    private var isShapeMoving = false

    private var clickedShape : Shape? = null

    private val handler = Handler(Looper.getMainLooper())
    private val onLongPressed = Runnable {
        selectedShape?.let {
            onShapeChanged?.onShapeLongClick(it)
        }
    }

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
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
        strokeWidth = 2.toPx
        color = Color.BLACK
        pathEffect = DashPathEffect(floatArrayOf(20.toPx, 5.toPx), 0f)
    }

    private fun createPaint() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
        strokeWidth = this@ShapesView.strokeWidth.toPx
        color = this@ShapesView.color
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null || geometryType == GeometryType.TEXT)
            return false

        val touchX = event.x
        val touchY = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (geometryType == GeometryType.HAND && selection?.contains(
                        touchX,
                        touchY
                    ) == true
                ) {
                    handler.postDelayed(onLongPressed, 1000)
                    startX = touchX
                    startY = touchY
                    isShapeMoving = true
                } else {
                    currentShape = when (geometryType) {
                        GeometryType.PATH -> CustomPath(createPaint())
                        GeometryType.LINE -> CustomLine(createPaint())
                        GeometryType.RECT -> CustomRectF(createPaint())
                        GeometryType.ELLIPSE -> CustomEllipse(createPaint())
                        GeometryType.ARROW -> CustomArrow(arrowWidth, arrowHeight, createPaint())
                        else -> null
                    }
                    currentShape?.down(touchX, touchY)
                    if (currentShape != null)
                        addNewShape(currentShape!!)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                handler.removeCallbacks(onLongPressed)
                if (geometryType == GeometryType.HAND && selection != null && isShapeMoving) {
                    val dx = touchX - startX
                    val dy = touchY - startY
                    selectedShape?.translate(dx, dy)
                    selection?.translate(dx, dy)
                    startX = touchX
                    startY = touchY
                    updateSelectionShader()
                }
                currentShape?.move(touchX, touchY)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(onLongPressed)
                if (geometryType == GeometryType.HAND) {
                    if (isShapeMoving) {
                        isShapeMoving = false
                        selectedShape?.up()
                    } else {
                        deselectShape()
                        selectedShape = shapes.lastOrNull { it.isInside(touchX, touchY) }
                        selectedShape?.let {
                            selection = CustomRectF(boundingBoxPaint, it.getBoundingBox())
                            updateSelectionShader()
                        }
                    }
                }
                currentShape?.up()
                currentShape = null
            }
            else -> return false
        }

        invalidate()
        return true
    }

    private fun updateSelectionShader() {
        selectedShape?.let {
            val shader =
                SweepGradient(selection!!.centerX(), selection!!.centerY(), selectionColors, null)
            it.applyShader(shader)
        }
    }

    private fun deselectShape() {
        selectedShape?.applyShader(null)
        selectedShape = null
        selection = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (shape in shapes.descendingIterator())
            shape.drawInCanvas(canvas)
        if (selection != null)
            canvas.drawRect(selection!!, boundingBoxPaint)
    }
}