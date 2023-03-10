package com.example.mobilepaint.drawing_view.shapes

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

class SelectionBorder(
    handlePaint : Paint,
    private val handleSize : Float,
    paint: Paint,
    rect: RectF
): CustomRectF(paint, rect) {

    private val handles = mutableListOf<CustomRectF>()

    init {
        handles.add(
            CustomRectF(handlePaint).apply {
                down(rect.left - handleSize / 2, rect.top - handleSize / 2)
                move(rect.left + handleSize / 2, rect.top + handleSize / 2)
                up(rect.left + handleSize / 2, rect.top + handleSize / 2)
            }
        )
        handles.add(
            CustomRectF(handlePaint).apply {
                down(rect.right - handleSize / 2, rect.top - handleSize / 2)
                move(rect.right + handleSize / 2, rect.top + handleSize / 2)
                up(rect.right + handleSize / 2, rect.top + handleSize / 2)
            }
        )
        handles.add(
            CustomRectF(handlePaint).apply {
                down(rect.left - handleSize / 2, rect.bottom - handleSize / 2)
                move(rect.left + handleSize / 2, rect.bottom + handleSize / 2)
                up(rect.left + handleSize / 2, rect.bottom + handleSize / 2)
            }
        )
        handles.add(
            CustomRectF(handlePaint).apply {
                down(rect.right - handleSize / 2, rect.bottom - handleSize / 2)
                move(rect.right + handleSize / 2, rect.bottom + handleSize / 2)
                up(rect.right + handleSize / 2, rect.bottom + handleSize / 2)
            }
        )
    }

    override fun drawInCanvas(canvas: Canvas) {
        super.drawInCanvas(canvas)
        handles.forEach {
            it.drawInCanvas(canvas)
        }
    }

    override fun translate(dx: Float, dy: Float) {
        super.translate(dx, dy)
        handles.forEach {
            it.translate(dx, dy)
        }
    }

    fun getHandleIndexByPoint(x: Float, y: Float) = handles.indexOfFirst { it.isInside(x, y) }

    override fun resize(dx: Float, dy: Float, handleIndex: Int) {
        super.resize(dx, dy, handleIndex)
        handles[handleIndex].translate(dx, dy)
        when (handleIndex) {
            0 -> {
                handles[1].translate(0f, dy)
                handles[2].translate(dx, 0f)
            }
            1 -> {
                handles[0].translate(0f, dy)
                handles[3].translate(dx, 0f)
            }
            2 -> {
                handles[0].translate(dx, 0f)
                handles[3].translate(0f, dy)
            }
            3 -> {
                handles[1].translate(dx, 0f)
                handles[2].translate(0f, dy)
            }
        }

        /*val handles1 = mutableListOf(handles)
        val index0 = handles.indexOfFirst {  }*/

        /*index = handleIndex

        when (handleIndex) {
            0 -> {
                // 0 -> 1
                if (
                    handles[0].centerX() > handles[1].centerX() &&
                    handles[0].centerX() > handles[3].centerX() &&
                    handles[0].centerY() < handles[3].centerY()
                ) {
                    index = 1
                    val handle0 = handles[0]
                    val handle1 = handles[1]
                    val handle2 = handles[2]
                    val handle3 = handles[3]
                    handles[0] = handle1
                    handles[1] = handle0
                    handles[2] = handle3
                    handles[3] = handle2
                } /*else if ( // 0 -> 2
                    handles[0].centerY() > handles[2].centerY() &&
                    handles[0].centerX() > handles[3].centerX() &&
                    handles[0].centerY() < handles[3].centerY()
                ) {
                    index = 2
                }*/
            }
        }*/

    }

}