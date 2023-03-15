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
    @AttrRes defStyleAttr: Int = 0,
): ZoomLayout(context, attrs, defStyleAttr) {

    var touchable = true
    var isTouchable : (() -> Boolean)? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return if (touchable) super.onTouchEvent(ev) else false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (touchable) super.onInterceptTouchEvent(ev) else false
    }
}