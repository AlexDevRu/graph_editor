package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*

class CustomText(
    override val paint: Paint,
    private val rotateAngle: Float,
    private val scale: Float
): Shape {

    private var x = 0f
    private var y = 0f

    private val textBounds = Rect()
    private val textBounds1 = Rect()

    private var textLines: List<String>? = null
    
    private var textWidth = 0f

    var text = ""
        set(value) {
            field = value
            paint.getTextBounds(field, 0, field.length, textBounds)
            textLines = text.split("\n").reversed()

            textWidth = if(!textLines.isNullOrEmpty()) {
                var maxTextLine = textLines!!.first()
                for(line in textLines!!)
                    if(line.length > maxTextLine.length)
                        maxTextLine = line

                paint.getTextBounds(maxTextLine, 0, maxTextLine.length, textBounds1)
                textBounds1.width().toFloat()
            } else {
                0f
            }
        }

    override fun drawInCanvas(canvas: Canvas) {
        if(!textLines.isNullOrEmpty()) {
            canvas.save()

            val th = paint.descent() - paint.ascent()

            canvas.rotate(rotateAngle, x + textWidth / 2, y - th * textLines!!.size / 2)
            canvas.scale(scale, scale, x + textWidth / 2, y - th / 2)

            var ty = y
            for(line in textLines!!) {
                canvas.drawText(line, x, ty, paint)
                ty -= paint.descent() - paint.ascent()
            }

            canvas.restore()
        }
    }

    override fun down(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    override fun move(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    override fun isInside(x: Float, y: Float): Boolean {
        return false
    }

    override fun changeColor(color: Int) {
        paint.color = color
    }

    override fun setSelected(selected: Boolean) {
        TODO("Not yet implemented")
    }
}