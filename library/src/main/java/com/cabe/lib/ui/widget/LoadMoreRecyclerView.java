package com.cabe.lib.ui.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cabe.lib.ui.loadmore.R;

/**
 * 作者：沈建芳 on 2019-05-16 14:45
 */
public class LoadMoreRecyclerView extends RecyclerView {
    private RecyclerViewScrollCallback scrollCallback;
    private InnerAdapter innerAdapter = new InnerAdapter();
    public LoadMoreRecyclerView(Context context) {
        this(context, null);
    }

    public LoadMoreRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadMoreRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LayoutManager layoutManager = getLayoutManager();
        if(layoutManager instanceof GridLayoutManager) {
            ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if(!innerAdapter.flagEnd && position == innerAdapter.getItemCount() - 1) {
                        return ((GridLayoutManager) layoutManager).getSpanCount();
                    }
                    return 1;
                }
            });
        }
        addOnScrollListener(new OnRcvScrollListener() {
            @Override
            protected void scrollStop() {
                if(!innerAdapter.flagEnd && isScrollBottom) {
                    if(scrollCallback != null) {
                        scrollCallback.onScrollToBottom();
                    }
                }
            }
        });
    }

    public void setScrollCallback(RecyclerViewScrollCallback scrollCallback) {
        this.scrollCallback = scrollCallback;
    }

    public void setScrollEnd(boolean flagEnd) {
        innerAdapter.setScrollEnd(flagEnd);
    }

    private void judgeDataFullPage() {
        postDelayed(() -> {
            RecyclerView.LayoutManager layoutManager = getLayoutManager();
            Rect rect = new Rect();
            int childPosition = layoutManager.getItemCount() > 1 ? layoutManager.getItemCount() - 2 : 0;
            View lastView = layoutManager.findViewByPosition(childPosition);
            if(lastView != null) {
                //lastView为空，表示没滚动到底
                lastView.getGlobalVisibleRect(rect);
                if(rect.bottom < getHeight()) {
                    if(scrollCallback != null) {
                        scrollCallback.onScrollToBottom();
                    }
                }
            }
        }, 400);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                innerAdapter.flagEnd = false;
                innerAdapter.notifyDataSetChanged();
                judgeDataFullPage();
            }
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                innerAdapter.notifyItemRangeChanged(positionStart, itemCount);
                judgeDataFullPage();
            }
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                innerAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
                judgeDataFullPage();
            }
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                innerAdapter.notifyItemRangeInserted(positionStart, itemCount);
                judgeDataFullPage();
            }
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                innerAdapter.notifyItemRangeRemoved(positionStart, itemCount);
                judgeDataFullPage();
            }
            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                innerAdapter.notifyItemMoved(fromPosition, toPosition);
                judgeDataFullPage();
            }
        };
        adapter.registerAdapterDataObserver(dataObserver);
        innerAdapter.setRealAdapter(adapter);
        super.setAdapter(innerAdapter);
    }

    private class InnerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private boolean flagEnd;
        private RecyclerView.Adapter<RecyclerView.ViewHolder> realAdapter;

        private void setScrollEnd(boolean flagEnd) {
            this.flagEnd = flagEnd;
        }

        private void setRealAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
            realAdapter = adapter;
            notifyDataSetChanged();
        }

        private int getRealCount() {
            return realAdapter == null ? 0 : realAdapter.getItemCount();
        }

        @Override
        public int getItemCount() {
            if(flagEnd) {
                return getRealCount();
            }
            return getRealCount() == 0 ? 0 : getRealCount() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            return position >= getRealCount() ? Integer.MIN_VALUE : realAdapter.getItemViewType(position);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType == Integer.MIN_VALUE) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_widget_bottom_loading_view, parent, false);
                return new LoadViewHolder(itemView);
            }
            return realAdapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(position < getRealCount()) {
                realAdapter.onBindViewHolder(holder, position);
            } else {
                ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                if(params instanceof StaggeredGridLayoutManager.LayoutParams) {
                    ((StaggeredGridLayoutManager.LayoutParams) params).setFullSpan(true);
                }
            }
        }
    }

    private class LoadViewHolder extends RecyclerView.ViewHolder {
        LoadViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface RecyclerViewScrollCallback {
        void onScrollToBottom();
    }
}
