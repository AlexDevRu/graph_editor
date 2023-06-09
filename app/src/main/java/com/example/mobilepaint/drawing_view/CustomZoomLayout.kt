package com.example.mobilepaint.drawing_view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.AttrRes
import com.otaliastudios.zoom.ZoomLayout

class CustomZoomLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): ZoomLayout(context, attrs, defStyle) {

    var touchable = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return if (touchable) super.onTouchEvent(ev) else false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (touchable) super.onInterceptTouchEvent(ev) else false
    }
}