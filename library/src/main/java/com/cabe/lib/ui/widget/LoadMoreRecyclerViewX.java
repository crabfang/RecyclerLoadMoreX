package com.cabe.lib.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cabe.lib.ui.loadmore.R;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * 作者：沈建芳 on 2019-05-16 14:45
 */
public class LoadMoreRecyclerViewX extends RecyclerView {
    private final int VIEW_TYPE_LOAD = Integer.MIN_VALUE + 1;
    private final int VIEW_TYPE_END = Integer.MIN_VALUE + 2;

    private boolean showEnd = true;
    private boolean autoLoad = true;
    private boolean flagEnd = false;
    private OnEndViewListener onEndViewListener;
    private OnLoadViewListener onLoadViewListener;
    private RecyclerViewScrollCallback scrollCallback;
    private OnChildComputeCallback childrenCallback;
    private InnerAdapter innerAdapter = new InnerAdapter();

    private String loadTips = "";
    private String endTips = "";

    public LoadMoreRecyclerViewX(Context context) {
        this(context, null);
    }

    public LoadMoreRecyclerViewX(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadMoreRecyclerViewX(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadMoreRecyclerViewX, defStyleAttr, 0);
        showEnd = a.getBoolean(R.styleable.LoadMoreRecyclerViewX_showEnd, true);
        autoLoad = a.getBoolean(R.styleable.LoadMoreRecyclerViewX_autoLoad, true);
        a.recycle();

        LayoutManager layoutManager = getLayoutManager();
        if(layoutManager instanceof GridLayoutManager) {
            ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if(!flagEnd && position == innerAdapter.getItemCount() - 1) {
                        return ((GridLayoutManager) layoutManager).getSpanCount();
                    }
                    return 1;
                }
            });
        }
        addOnScrollListener(new OnRcvScrollListener() {
            @Override
            protected void scrollStop() {
                if(autoLoad && !flagEnd && isScrollBottom) {
                    if(scrollCallback != null) {
                        scrollCallback.onScrollToBottom();
                    }
                }
            }
        });
    }

    public void setOnEndViewListener(OnEndViewListener onEndViewListener) {
        this.onEndViewListener = onEndViewListener;
    }

    public void setOnLoadViewListener(OnLoadViewListener onLoadViewListener) {
        this.onLoadViewListener = onLoadViewListener;
    }

    public void setScrollCallback(RecyclerViewScrollCallback scrollCallback) {
        this.scrollCallback = scrollCallback;
    }

    public void setOnChildrenCallback(OnChildComputeCallback childrenCallback) {
        this.childrenCallback = childrenCallback;
    }

    public void setLoadTips(String loadTips) {
        this.loadTips = loadTips;
    }

    public void setEndTips(String endTips) {
        this.endTips = endTips;
    }

    public void setAutoLoad(boolean autoLoad) {
        this.autoLoad = autoLoad;
    }

    public void setShowEnd(boolean showEnd) {
        this.showEnd = showEnd;
    }

    public void setScrollEnd(boolean flagEnd) {
        this.flagEnd = flagEnd;
    }

    private void judgeDataFullPage() {
        if(!autoLoad) return;

        postDelayed(() -> {
            boolean isEnough = true;
            RecyclerView.LayoutManager layoutManager = getLayoutManager();
            Rect rect = new Rect();
            assert layoutManager != null;
            int childPosition = layoutManager.getItemCount() > 1 ? layoutManager.getItemCount() - 2 : 0;
            View lastView = layoutManager.findViewByPosition(childPosition);
            if(lastView != null) {
                //lastView为空，表示没滚动到底
                lastView.getGlobalVisibleRect(rect);
                if(rect.bottom <= layoutManager.getHeight() && !flagEnd) {
                    isEnough = false;
                    if(scrollCallback != null) {
                        scrollCallback.onScrollToBottom();
                    }
                }
            }
            if(childrenCallback != null) {
                isEnough = childrenCallback.isChildrenNotEnough(this);
            }
            if(!isEnough && scrollCallback != null) {
                scrollCallback.onScrollToBottom();
            }
        }, 200);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                innerAdapter.notifyDataSetChanged();
                judgeDataFullPage();
            }
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                innerAdapter.notifyItemRangeChanged(positionStart, itemCount);
                judgeDataFullPage();
                if(itemCount == 0) {
                    innerAdapter.notifyItemChanged(innerAdapter.getItemCount() - 1);
                }
            }
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                innerAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
                judgeDataFullPage();
                if(itemCount == 0) {
                    innerAdapter.notifyItemChanged(innerAdapter.getItemCount() - 1);
                }
            }
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                innerAdapter.notifyItemRangeInserted(positionStart, itemCount);
                judgeDataFullPage();
                if(itemCount == 0) {
                    innerAdapter.notifyItemChanged(innerAdapter.getItemCount() - 1);
                }
            }
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                innerAdapter.notifyItemRangeRemoved(positionStart, itemCount);
                judgeDataFullPage();
                if(itemCount == 0) {
                    innerAdapter.notifyItemChanged(innerAdapter.getItemCount() - 1);
                }
            }
            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                innerAdapter.notifyItemMoved(fromPosition, toPosition);
                judgeDataFullPage();
                if(itemCount == 0) {
                    innerAdapter.notifyItemChanged(innerAdapter.getItemCount() - 1);
                }
            }
        };
        adapter.registerAdapterDataObserver(dataObserver);
        innerAdapter.setRealAdapter(adapter);
        super.setAdapter(innerAdapter);
    }

    private class InnerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private RecyclerView.Adapter<RecyclerView.ViewHolder> realAdapter;

        private void setRealAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
            realAdapter = adapter;
            notifyDataSetChanged();
        }

        private int getRealCount() {
            return realAdapter == null ? 0 : realAdapter.getItemCount();
        }

        @Override
        public int getItemCount() {
            if(!autoLoad || (flagEnd && !showEnd)) {
                return getRealCount();
            }
            return getRealCount() == 0 ? 0 : getRealCount() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if(getRealCount() != getItemCount() && position == getRealCount()) {
                if(!flagEnd) {
                    return VIEW_TYPE_LOAD;
                }
                if(showEnd) {
                    return VIEW_TYPE_END;
                }
            }
            return realAdapter.getItemViewType(position);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType == VIEW_TYPE_LOAD) {
                return LoadViewHolder.createHolder(parent, onLoadViewListener);
            } else if(viewType == VIEW_TYPE_END) {
                return EndViewHolder.createHolder(parent, onEndViewListener);
            }
            return realAdapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(position < getRealCount()) {
                realAdapter.onBindViewHolder(holder, position);
            } else {
                String customLabelStr = "";
                if(holder instanceof LoadViewHolder) {
                    ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                    if(params instanceof StaggeredGridLayoutManager.LayoutParams) {
                        ((StaggeredGridLayoutManager.LayoutParams) params).setFullSpan(true);
                    }
                    if(onLoadViewListener != null) {
                        onLoadViewListener.onLoadViewBind(holder.itemView);
                    }
                    customLabelStr = loadTips;
                } else if(holder instanceof EndViewHolder) {
                    if(onEndViewListener != null) {
                        onEndViewListener.onEndViewBind(holder.itemView);
                    }
                    customLabelStr = endTips;
                }
                if(!TextUtils.isEmpty(customLabelStr)) {
                    TextView label = holder.itemView.findViewById(R.id.load_more_widget_bottom_end_label);
                    if(label != null) {
                        label.setText(customLabelStr);
                    }
                }
            }
        }
    }

    public interface RecyclerViewScrollCallback {
        void onScrollToBottom();
    }
    public interface OnChildComputeCallback {
        boolean isChildrenNotEnough(LoadMoreRecyclerViewX recyclerView);
    }
}
