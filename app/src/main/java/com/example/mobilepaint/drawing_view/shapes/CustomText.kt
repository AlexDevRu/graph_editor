package com.example.mobilepaint.drawing_view.shapes

import android.graphics.*
import com.example.mobilepaint.drawing_view.Operation
import com.example.mobilepaint.models.json.ShapeData
import com.example.mobilepaint.models.json.TextData
import com.google.gson.Gson
import com.otaliastudios.opengl.geometry.RectF

class CustomText(
    override val paint: Paint,
    val rotateAngle: Float,
    val scale: Float
): Shape {

    private val path = Path()

    constructor(basePaint: Paint, textData: TextData) : this(
        basePaint.apply {
            color = textData.shapeData.color
            textSize = textData.shapeData.stroke
        },
        textData.rotateAngle,
        textData.scale
    ) {
        x = textData.x
        y = textData.y
        text = textData.text
    }

    var x = 0f
        private set

    var y = 0f
        private set

    private val textBounds = Rect()
    private val textBounds1 = Rect()

    private var textLines: List<String>? = null
    
    private var textWidth = 0f
    private var textHeight = 0f

    private val matrix = Matrix()

    private val paint1 = Paint(paint).apply { color = Color.RED }
    private val paint2 = Paint(paint).apply { color = Color.GREEN }

    var text = ""
        set(value) {
            field = value

            textLines = field.split("\n").reversed()

            textWidth = 0f
            textHeight = 0f

            textLines?.forEach {
                paint.getTextBounds(it, 0, it.length, textBounds)
                if (textBounds.width() > textWidth)
                    textWidth = textBounds.width().toFloat()
                textHeight += textBounds.height()
            }

            calculatePoints()

            /*textWidth = if(!textLines.isNullOrEmpty()) {
                var maxTextLine = textLines!!.first()
                for(line in textLines!!)
                    if(line.length > maxTextLine.length)
                        maxTextLine = line

                paint.getTextBounds(maxTextLine, 0, maxTextLine.length, textBounds1)
                textBounds1.width().toFloat()
            } else {
                0f
            }*/
        }

    private fun calculatePoints() {
        val cx = x + textWidth / 2
        val cy = y - textHeight / 2

        path.reset()
        path.moveTo(x, y)
        path.lineTo(x, y - textHeight)
        path.lineTo(x + textWidth, y - textHeight)
        path.lineTo(x + textWidth, y)
        path.close()

        matrix.reset()
        matrix.setRotate(rotateAngle, cx, cy)
        matrix.postScale(scale, scale, cx, cy)

        path.transform(matrix)

        path.computeBounds(bounds, true)
    }

    override fun drawInCanvas(canvas: Canvas) {
        if(!textLines.isNullOrEmpty() && !selected) {
            //canvas.drawPath(path, paint1)

            canvas.save()

            val th = paint.descent() - paint.ascent()

            val cx = x + textWidth / 2
            val cy = y - textHeight / 2
            canvas.rotate(rotateAngle, cx, cy)
            canvas.scale(scale, scale, cx, cy)

            var ty = y
            for(line in textLines!!) {
                canvas.drawText(line, x, ty, paint)
                ty -= paint.descent() - paint.ascent()
            }

            canvas.restore()
        }
    }

    private var selected = false

    override fun down(x: Float, y: Float) {
        if (!selected) {
            this.x = x
            this.y = y
            calculatePoints()
        }
    }

    override fun move(x: Float, y: Float) {
        if (!selected) {
            this.x = x
            this.y = y
            calculatePoints()
        }
    }

    private val bounds = RectF()

    override fun isInside(x: Float, y: Float): Boolean {
        return bounds.contains(x, y)
    }

    override fun changeColor(color: Int) {
        paint.color = color
    }

    override fun setSelected(selected: Boolean) {
        this.selected = selected
    }

    override fun toJson(gson: Gson): String {
        val shapeData = ShapeData(paint.color, null, paint.textSize)
        val textData = TextData(
            shapeData = shapeData,
            x = x,
            y = y,
            text = text,
            rotateAngle = rotateAngle,
            scale = scale
        )
        return gson.toJson(textData)
    }
}