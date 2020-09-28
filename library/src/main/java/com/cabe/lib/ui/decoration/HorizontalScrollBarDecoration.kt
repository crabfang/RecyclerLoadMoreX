package com.cabe.lib.ui.decoration

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView

class HorizontalScrollBarDecoration: RecyclerView.ItemDecoration() {
    private var parent: RecyclerView? = null
    private fun dp2px(dp: Float): Int {
        return (parent?.let {
            it.context.resources.displayMetrics.density * dp
        } ?: dp).toInt()
    }
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        this.parent = parent

        val barHeight = dp2px(2f)
        val scrollWidth = dp2px(20f)
        val indicatorWidth = dp2px(6f)
        val paddingBottom = dp2px(9f)
        val barX = (parent.width / 2 - scrollWidth / 2).toFloat()
        val barY = (parent.height - paddingBottom - barHeight).toFloat()

        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = Color.parseColor("#FFEAF1FE")
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = barHeight.toFloat()

        val extent = parent.computeHorizontalScrollExtent()
        val range = parent.computeHorizontalScrollRange()
        val offset = parent.computeHorizontalScrollOffset()
        val maxEndX = (range - extent).toFloat()
        //可滑动
        if (maxEndX > 0) {
            c.drawLine(barX, barY, barX + scrollWidth.toFloat(), barY, paint)
            val proportion = offset / maxEndX

            val scrollableDistance = scrollWidth - indicatorWidth

            val offsetX = scrollableDistance * proportion
            paint.color = Color.parseColor("#FF327BF9")
            c.drawLine(barX + offsetX, barY, barX + indicatorWidth.toFloat() + offsetX, barY, paint)
        }
    }
}