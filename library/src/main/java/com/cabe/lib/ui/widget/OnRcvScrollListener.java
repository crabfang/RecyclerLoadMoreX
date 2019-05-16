package com.cabe.lib.ui.widget;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * RecyclerView Scroll Listener
 * 作者：沈建芳 on 16/9/22 15:29
 */
public abstract class OnRcvScrollListener extends RecyclerView.OnScrollListener {
    protected boolean isScrollBottom;

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        boolean canScroll = recyclerCheckScroll(recyclerView);

        if(newState != RecyclerView.SCROLL_STATE_IDLE) {
            isScrollBottom = false;
        }

        //当前RecyclerView显示出来的最后一个的item的position
        int lastPosition = -1;
        //当前状态为停止滑动状态SCROLL_STATE_IDLE时
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            if (layoutManager instanceof GridLayoutManager) {
                //通过LayoutManager找到当前显示的最后的item的position
                lastPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof LinearLayoutManager) {
                lastPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                //因为StaggeredGridLayoutManager的特殊性可能导致最后显示的item存在多个，所以这里取到的是一个数组
                //得到这个数组后再取到数组中position值最大的那个就是最后显示的position值了
                int[] lastPositions = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(lastPositions);
                lastPosition = findMax(lastPositions);
            }

            //时判断界面显示的最后item的position是否等于itemCount总数-1也就是最后一个item的position
            //如果相等则说明已经滑动到最后了
            if (canScroll && lastPosition > 0 && lastPosition >= layoutManager.getItemCount() - 1) {
                isScrollBottom = true;
            }

            scrollStop();
        }
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    protected abstract void scrollStop();

    private boolean recyclerCheckScroll(RecyclerView recyclerView) {
        //向上拖到，滚动到底部方向
        int directDown = 1;
        //向下拖动，滚动到顶部方向
        int directUp = -1;
        boolean canScrollToBottom = recyclerView.canScrollVertically(directDown);
        boolean canScrollToTop = recyclerView.canScrollVertically(directUp);

        return canScrollToBottom || canScrollToTop;
    }
}
