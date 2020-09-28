package com.cabe.lib.ui.decoration

import android.graphics.*
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class FastIndexByLetterDecoration(val onLetterChange: (letter: String) -> Unit): RecyclerView.ItemDecoration() {
    private lateinit var boundRect: RectF
    private var preHeight = 0
    private var letterH = 0f
    private var floatTouchListener: MyTouchListener? = null
    private val letterList = arrayListOf<String>().apply {
        for (i in 0 until 26) {
            add(('a' + i).toString().toUpperCase(Locale.getDefault()))
        }
    }
    private var preLetter = ""
    private var parent: RecyclerView? = null
    private fun dp2px(dp: Float): Int {
        return (parent?.let {
            it.context.resources.displayMetrics.density * dp
        } ?: dp).toInt()
    }
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        this.parent = parent
        if(letterH == 0f) letterH = dp2px(12f).toFloat()

        if(floatTouchListener != null) {
            parent.removeOnItemTouchListener(floatTouchListener!!)
        }

        boundRect = RectF()
        boundRect.left = parent.width - dp2px(20f).toFloat()
        boundRect.right = parent.width.toFloat()
        boundRect.top = dp2px(40f).toFloat()
        boundRect.bottom = parent.height.toFloat()

        val paint = Paint()
        paint.isAntiAlias = true

        paint.color = Color.parseColor("#327BF9")
        paint.style = Paint.Style.FILL
        paint.textSize = dp2px(10f).toFloat()

        letterList.forEachIndexed { index, letter ->
            val textBound = Rect()
            paint.getTextBounds(letter, 0, letter.length, textBound)

            val textOffsetX = dp2px(4f)
            val textOffsetY = dp2px(2f)

            val drawX = boundRect.left + textOffsetX
            val drawY = boundRect.top + letterH * index + textOffsetY
            c.drawText(letter, drawX, drawY, paint)
        }

        if(floatTouchListener == null || preHeight == 0 || preHeight != parent.height) {
            preHeight = parent.height
            floatTouchListener = MyTouchListener()
        }
        parent.addOnItemTouchListener(floatTouchListener!!)
    }

    private fun calPosition(event: MotionEvent?) {
        event?.let {
            val touchX = event.x
            val touchY = event.y
            if(boundRect.contains(touchX, touchY)) {
                (touchY - boundRect.top).let { offset ->
                    (offset / letterH).toInt().let { index ->
                        if(index >= 0 && index < letterList.size) {
                            val curLetter = letterList[index]
                            if(curLetter != preLetter) {
                                preLetter = curLetter
                                onLetterChange(curLetter)
                            }
                        }
                    }
                }
            }
        }
    }

    private inner class MyTouchListener: RecyclerView.OnItemTouchListener {
        private var fastIntercept = false
        private fun handleEvent(e: MotionEvent?) {
            val touchX = e?.x ?: 0f
            val touchY = e?.y ?: 0f
            when(e?.action) {
                MotionEvent.ACTION_DOWN -> {
                    if(boundRect.contains(touchX, touchY)) fastIntercept = true
                }
                MotionEvent.ACTION_MOVE -> calPosition(e)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> fastIntercept = false
            }
        }
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            handleEvent(e)
            return fastIntercept
        }
        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
            handleEvent(e)
        }
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    }
}