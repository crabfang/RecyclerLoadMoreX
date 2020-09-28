package com.cabe.lib.ui.decoration

import android.graphics.*
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class FloatButtonDecoration(val click: (() -> Unit)? = null): RecyclerView.ItemDecoration() {
    private lateinit var ovalRect: RectF
    private var preHeight = 0
    private var floatTouchListener: MyTouchListener? = null
    private var parent: RecyclerView? = null
    private fun dp2px(dp: Float): Int {
        return (parent?.let {
            it.context.resources.displayMetrics.density * dp
        } ?: dp).toInt()
    }
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        this.parent = parent

        if(floatTouchListener != null) {
            parent.removeOnItemTouchListener(floatTouchListener!!)
        }
        val scrollV = parent.layoutManager?.computeVerticalScrollOffset(state) ?: 0

        if(scrollV > parent.height) {
            val paint = Paint()
            paint.isAntiAlias = true
            paint.color = Color.parseColor("#FFFFFFFF")
            paint.style = Paint.Style.FILL
            val radius = dp2px(20f).toFloat()
            val offsetX = parent.width - dp2px(60f).toFloat()
            val offsetY = parent.height - dp2px(100f).toFloat()
            ovalRect = RectF(offsetX, offsetY, offsetX + radius * 2, offsetY + radius * 2)
            c.drawOval(ovalRect, paint)

            paint.color = Color.parseColor("#FFCCCCCC")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            c.drawOval(ovalRect, paint)

            paint.color = Color.parseColor("#FF666666")
            paint.style = Paint.Style.FILL
            paint.textSize = dp2px(12f).toFloat()

            val btnStr = "顶部"
            val textBound = Rect()
            paint.getTextBounds(btnStr, 0, btnStr.length, textBound)
            val textOffsetX = (radius * 2 - textBound.width()) / 2
            val textOffsetY = (radius * 2 - textBound.height()) / 2
            c.drawText(btnStr, ovalRect.left + textOffsetX, ovalRect.top + textOffsetY + textBound.height() * 9 / 10, paint)

            if(floatTouchListener == null || preHeight == 0 || preHeight != parent.height) {
                preHeight = parent.height
                floatTouchListener = MyTouchListener(parent, ovalRect)
            }
            parent.addOnItemTouchListener(floatTouchListener!!)
        }
    }

    private inner class MyTouchListener(parent: RecyclerView, ovalRect: RectF): RecyclerView.OnItemTouchListener {
        val mTapDetector = GestureDetector(parent.context, SingleTapDetector(ovalRect))
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            return mTapDetector.onTouchEvent(e)
        }
        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        }
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        }
    }

    private inner class SingleTapDetector(val ovalRect: RectF): GestureDetector.SimpleOnGestureListener() {
        private fun isFloatArea(event: MotionEvent?): Boolean {
            if(event == null) return false

            var result = false
            val touchX = event.x
            val touchY = event.y
            if(ovalRect.contains(touchX, touchY)) {
                result = true
            }
            return result
        }
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (isFloatArea(e)) {
                click?.invoke() ?: parent?.scrollToPosition(0)
                return true
            }
            return false
        }
        override fun onDoubleTap(e: MotionEvent): Boolean {
            return true
        }
    }
}