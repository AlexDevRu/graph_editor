package com.example.mobilepaint.drawing_view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.mobilepaint.Utils.toPx
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    val path = Path().apply {
        moveTo(600f, 10f)
        lineTo(600f, 10f)
        lineTo(100f, 100f)
        lineTo(200f, 500f)
        lineTo(600f, 900f)
    }

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4.toPx
        color = Color.BLACK
    }

    private var sx = 1f
    private var sy = 1f

    private val matrix1 = Matrix()
    private val matrix2 = Matrix()
    private val matrix3 = Matrix()

    private val bounds = RectF()

    init {
        GlobalScope.launch {
            delay(2000)
            matrix1.setTranslate(100f, 100f)
            path.transform(matrix1)
            invalidate()
            delay(1000)
            matrix1.setTranslate(100f, 100f)
            path.transform(matrix1)
            invalidate()
            delay(1000)
            matrix1.setTranslate(100f, 100f)
            path.transform(matrix1)
            invalidate()
            delay(1000)
            matrix2.setScale(1.2f, 1.2f, bounds.left, bounds.top)
            path.transform(matrix2)
            invalidate()
            delay(1000)
            path.computeBounds(bounds, true)
            matrix3.setRotate(45f, bounds.centerX(), bounds.centerY())
            path.transform(matrix3)
            invalidate()
            delay(1000)
            path.computeBounds(bounds, true)
            matrix3.setRotate(45f, bounds.centerX(), bounds.centerY())
            path.transform(matrix3)
            invalidate()
            /*delay(1000)
            matrix2.setScale(0.5f, 0.5f, bounds.left, bounds.top)
            path.transform(matrix2)
            invalidate()*/
            /*delay(1000)
            path.computeBounds(bounds, true)
            matrix2.invert(inverse)
            path.transform(inverse)
            matrix2.setScale(0.4f, 0.4f, bounds.left, bounds.top)
            path.transform(matrix2)
            invalidate()
            delay(1000)
            path.computeBounds(bounds, true)
            matrix2.invert(inverse)
            path.transform(inverse)
            matrix2.setScale(0.8f, 0.8f, bounds.right, bounds.bottom)
            path.transform(matrix2)
            invalidate()*/
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(path, paint)
    }

}