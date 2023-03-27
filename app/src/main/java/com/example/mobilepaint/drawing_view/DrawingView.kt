package com.example.mobilepaint.drawing_view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.example.mobilepaint.Utils.toPx
import com.example.mobilepaint.databinding.ViewDrawingViewBinding
import com.example.mobilepaint.drawing_view.shapes.CustomText
import com.example.mobilepaint.drawing_view.shapes.Shape
import com.google.gson.Gson
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin


@SuppressLint("ClickableViewAccessibility")
class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val binding = ViewDrawingViewBinding.inflate(LayoutInflater.from(context), this)

    private val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    var geometryType: GeometryType = GeometryType.TEXT
        set(value) {
            field = value
            binding.shapesView.geometryType = field
            closeEditText()
        }

    val hasModifications get() = binding.shapesView.hasModifications

    private var textMoved = false
    private val lastMovePoint = Point()

    val shapes : List<Shape>
        get() = binding.shapesView.shapes

    val removedShapes : List<Shape>
        get() = binding.shapesView.removedShapes

    var strokeWidth = 1f
        set(value) {
            field = value
            binding.shapesView.strokeWidth = field
        }

    private val editTextTouchListener = OnTouchListener { _, event ->
        val x = event.rawX
        val y = event.rawY
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                textMoved = true
                lastMovePoint.x = x.toInt()
                lastMovePoint.y = y.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                if(textMoved) {
                    val lp = binding.editText.layoutParams as LayoutParams
                    lp.leftMargin += (x.toInt() - lastMovePoint.x)
                    lp.topMargin += (y.toInt() - lastMovePoint.y)
                    binding.editText.layoutParams = lp
                    lastMovePoint.x = x.toInt()
                    lastMovePoint.y = y.toInt()
                }
            }
            MotionEvent.ACTION_UP -> {
                textMoved = false
            }
            else -> {
                textMoved = false
            }
        }
        true
    }

    private var centerX = 0f
    private var centerY = 0f
    private var startR = 0f
    private var startScale = 0f
    private var startX = 0f
    private var startY = 0f
    private var startRotation = 0f
    private var startA = 0f

    private var textInEdit = false

    private fun saveCurrentText() {
        val screenCoordinates = intArrayOf(0, 0)
        val screenCoordinatesShapesView = intArrayOf(0, 0)

        screenCoordinates[0] = binding.editText.left
        screenCoordinates[1] = binding.editText.bottom

        val paddingEditTextPx = binding.editText.paddingStart
        val paddingEditTextTopPx = binding.editText.paddingBottom

        val x = screenCoordinates[0] - screenCoordinatesShapesView[0] + paddingEditTextPx
        val y = screenCoordinates[1] - screenCoordinatesShapesView[1] - paddingEditTextTopPx - 3.toPx

        val textPaint = Paint()
        textPaint.set(binding.editText.paint)

        if (textInEdit) {
            binding.shapesView.shapes.remove(binding.shapesView.selectedShape)
            textInEdit = false
        }

        val textShape = CustomText(
            textPaint,
            binding.editText.rotation,
            binding.editText.scaleX
        )
        textShape.text = binding.editText.text.toString()

        textShape.down(x.toFloat(), y.toFloat())
        binding.shapesView.addNewShape(textShape)
        binding.shapesView.invalidate()

        closeEditText()
    }

    init {
        closeEditText()

        binding.editText.doAfterTextChanged {
            setPositionForButtons(binding.editText.scaleX, binding.editText.rotation)
        }

        binding.editText.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                saveCurrentText()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        binding.close.setOnClickListener {
            closeEditText()
            binding.shapesView.deselectShape()
        }
        binding.save.setOnClickListener {
            saveCurrentText()
        }

        binding.overlay.setOnTouchListener(editTextTouchListener)

        binding.expand.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    centerX = (binding.editText.left + binding.editText.right) / 2f
                    centerY = (binding.editText.top + binding.editText.bottom) / 2f
                    startX = e.rawX - binding.expand.x + centerX
                    startY = e.rawY - binding.expand.y + centerY
                    startR = hypot(e.rawX - startX, e.rawY - startY)
                    startA = Math.toDegrees(
                        atan2(
                            e.rawY - startY,
                            e.rawX - startX
                        ).toDouble()
                    ).toFloat()
                    startScale = binding.editText.scaleX
                    startRotation = binding.editText.rotation
                }
                MotionEvent.ACTION_MOVE -> {
                    val newR = hypot(e.rawX - startX, e.rawY - startY)
                    val newA = Math.toDegrees(
                        atan2(
                            e.rawY - startY,
                            e.rawX - startX
                        ).toDouble()
                    ).toFloat()
                    val newScale = newR / startR * startScale
                    val newRotation = newA - startA + startRotation
                    binding.editText.scaleX = newScale
                    binding.editText.scaleY = newScale
                    binding.editText.rotation = newRotation

                    binding.overlay.scaleX = newScale
                    binding.overlay.scaleY = newScale
                    binding.overlay.rotation = newRotation


                    val halfDiag = hypot(
                        binding.editText.width.toFloat(),
                        binding.editText.height.toFloat()
                    ) / 2f * newScale

                    val diagAngle = atan2(
                        binding.editText.height.toDouble(), binding.editText.width.toDouble()
                    ).toFloat()
                    val rotateInRadians = Math.toRadians(newRotation.toDouble()).toFloat()

                    binding.expand.x = centerX + halfDiag * cos(rotateInRadians + diagAngle)
                    binding.expand.y = centerY + halfDiag * sin(rotateInRadians + diagAngle)

                    binding.close.x = centerX - halfDiag * cos(rotateInRadians + diagAngle) - binding.close.width
                    binding.close.y = centerY - halfDiag * sin(rotateInRadians + diagAngle) - binding.close.height

                    binding.save.x = centerX + halfDiag * cos(rotateInRadians - diagAngle)
                    binding.save.y = centerY + halfDiag * sin(rotateInRadians - diagAngle) - binding.save.height


                    binding.expand.pivotX = 0f
                    binding.expand.pivotY = 0f
                    binding.expand.rotation = newRotation

                    binding.close.pivotX = binding.close.width.toFloat()
                    binding.close.pivotY = binding.close.height.toFloat()
                    binding.close.rotation = newRotation

                    binding.save.pivotX = 0f
                    binding.save.pivotY = binding.save.height.toFloat()
                    binding.save.rotation = newRotation
                }
                MotionEvent.ACTION_UP -> {
                }
            }
            true
        }

        binding.shapesView.onTextSelected = { textShape ->
            textInEdit = true
            val x = textShape.x
            val y = textShape.y
            val lp = binding.editText.layoutParams as LayoutParams
            lp.leftMargin = x.toInt()
            lp.topMargin = y.toInt() - binding.editText.height
            binding.editText.rotation = textShape.rotateAngle
            binding.editText.scaleX = textShape.scale
            binding.editText.scaleY = textShape.scale
            setPositionForButtons(textShape.scale, textShape.rotateAngle)

            binding.expand.pivotX = 0f
            binding.expand.pivotY = 0f
            binding.expand.rotation = textShape.rotateAngle

            binding.close.pivotX = binding.close.width.toFloat()
            binding.close.pivotY = binding.close.height.toFloat()
            binding.close.rotation = textShape.rotateAngle

            binding.save.pivotX = 0f
            binding.save.pivotY = binding.save.height.toFloat()
            binding.save.rotation = textShape.rotateAngle

            binding.buttons.isVisible = true
            binding.editText.isVisible = true
            binding.editText.setText(textShape.text)
            binding.editText.setSelection(textShape.text.length)
            binding.editText.requestFocus()
            imm.showSoftInput(binding.editText, 0)
        }
    }

    private fun setPositionForButtons(newScale: Float, newRotation: Float) {
        val cx = (binding.editText.left + binding.editText.right) / 2f
        val cy = (binding.editText.top + binding.editText.bottom) / 2f

        val halfDiag = hypot(
            binding.editText.width.toFloat(),
            binding.editText.height.toFloat()
        ) / 2f * newScale

        val diagAngle = atan2(
            binding.editText.height.toDouble(), binding.editText.width.toDouble()
        ).toFloat()
        val rotateInRadians = Math.toRadians(newRotation.toDouble()).toFloat()

        binding.expand.x = cx + halfDiag * cos(rotateInRadians + diagAngle)
        binding.expand.y = cy + halfDiag * sin(rotateInRadians + diagAngle)

        binding.close.x = cx - halfDiag * cos(rotateInRadians + diagAngle) - binding.close.width
        binding.close.y = cy - halfDiag * sin(rotateInRadians + diagAngle) - binding.close.height

        binding.save.x = cx + halfDiag * cos(rotateInRadians - diagAngle)
        binding.save.y = cy + halfDiag * sin(rotateInRadians - diagAngle) - binding.save.height
    }

    @ColorInt
    var color = Color.BLACK
        set(value) {
            field = value
            binding.shapesView.color = field
            binding.editText.setTextColor(field)
        }

    private fun closeEditText() {
        binding.editText.setText("")
        binding.editText.rotation = 0f
        binding.editText.scaleX = 1f
        binding.editText.scaleY = 1f

        binding.overlay.rotation = 0f
        binding.overlay.scaleX = 1f
        binding.overlay.scaleY = 1f

        binding.expand.rotation = 0f
        binding.expand.translationX = 0f
        binding.expand.translationY = 0f

        binding.save.rotation = 0f
        binding.save.translationX = 0f
        binding.save.translationY = 0f

        binding.close.rotation = 0f
        binding.close.translationX = 0f
        binding.close.translationY = 0f

        binding.editText.isVisible = false
        binding.buttons.isVisible = false
        imm.hideSoftInputFromWindow(binding.editText.windowToken, 0)
    }

    fun undo() {
        binding.shapesView.undo()
    }

    fun redo() {
        binding.shapesView.redo()
    }

    fun setOnShapeChangedListener(onShapeChanged: ShapesView.OnShapeChanged?) {
        binding.shapesView.setOnShapeChangedListener(onShapeChanged)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(geometryType == GeometryType.TEXT) {
            val touchX = event.x
            val touchY = event.y

            when(event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    val lp = binding.editText.layoutParams as LayoutParams
                    lp.leftMargin = touchX.toInt()
                    lp.topMargin = touchY.toInt()
                    binding.buttons.isVisible = true
                    binding.editText.isVisible = true
                    binding.editText.setSelection(0)
                    binding.editText.requestFocus()
                    imm.showSoftInput(binding.editText, 0)
                }
            }
        }

        return geometryType == GeometryType.TEXT
    }

    fun getBitmap() = binding.shapesView.getBitmap()

    fun fromJson(json : String, gson: Gson) = binding.shapesView.fromJson(binding.editText.paint, json, gson)

    fun addShapes(shapes: List<Shape>, removedShapes : List<Shape>) = binding.shapesView.addShapes(shapes, removedShapes)

    fun addBitmap(bitmap: Bitmap) = binding.shapesView.addBitmap(bitmap)

    fun removeShape(shape: Shape) = binding.shapesView.removeShape(shape)
}