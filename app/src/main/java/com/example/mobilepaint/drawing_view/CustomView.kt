package com.example.mobilepaint.drawing_view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.mobilepaint.Utils.toPx

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
        strokeWidth = 5.toPx
        color = Color.BLACK
    }

    val matrix1 = Matrix()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        matrix1.reset()
        matrix1.setScale(-0.1f, 1f, 10f, 10f)
        //path.transform(matrix1)
        canvas?.drawPath(path, paint)
    }

}