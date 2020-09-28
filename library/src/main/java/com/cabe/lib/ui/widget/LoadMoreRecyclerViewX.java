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
    private final static int VIEW_TYPE_LOAD = Integer.MIN_VALUE + 1;
    private final static int VIEW_TYPE_END = Integer.MIN_VALUE + 2;

    private boolean showEnd = true;
    private boolean autoLoad = true;
    private boolean flagEnd = false;
    private OnEndViewListener onEndViewListener;
    private OnLoadViewListener onLoadViewListener;
    private RecyclerViewScrollCallback scrollCallback;
    private OnChildComputeCallback childrenCallback;
    private ProxyAdapter proxyAdapter;

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
        proxyAdapter = new ProxyAdapter(this);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadMoreRecyclerViewX, defStyleAttr, 0);
        showEnd = a.getBoolean(R.styleable.LoadMoreRecyclerViewX_showEnd, true);
        autoLoad = a.getBoolean(R.styleable.LoadMoreRecyclerViewX_autoLoad, true);
        a.recycle();

        LayoutManager layoutManager = getLayoutManager();
        if(layoutManager instanceof GridLayoutManager) {
            ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if(!flagEnd && position == proxyAdapter.getItemCount() - 1) {
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
        if(adapter == null) {
            super.setAdapter(null);
            return;
        }
        RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                proxyAdapter.notifyDataSetChanged();
                judgeDataFullPage();
            }
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                proxyAdapter.notifyItemRangeChanged(positionStart, itemCount);
                judgeDataFullPage();
                if(itemCount == 0) {
                    proxyAdapter.notifyItemChanged(proxyAdapter.getItemCount() - 1);
                }
            }
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                proxyAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
                judgeDataFullPage();
                if(itemCount == 0) {
                    proxyAdapter.notifyItemChanged(proxyAdapter.getItemCount() - 1);
                }
            }
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                proxyAdapter.notifyItemRangeInserted(positionStart, itemCount);
                judgeDataFullPage();
                if(itemCount == 0) {
                    proxyAdapter.notifyItemChanged(proxyAdapter.getItemCount() - 1);
                }
            }
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                proxyAdapter.notifyItemRangeRemoved(positionStart, itemCount);
                judgeDataFullPage();
                if(itemCount == 0) {
                    proxyAdapter.notifyItemChanged(proxyAdapter.getItemCount() - 1);
                }
            }
            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                proxyAdapter.notifyItemMoved(fromPosition, toPosition);
                judgeDataFullPage();
                if(itemCount == 0) {
                    proxyAdapter.notifyItemChanged(proxyAdapter.getItemCount() - 1);
                }
            }
        };
        adapter.registerAdapterDataObserver(dataObserver);
        proxyAdapter.setRealAdapter(adapter);
        super.setAdapter(proxyAdapter);
    }

    public RecyclerView.Adapter<RecyclerView.ViewHolder> getRealAdapter() {
        return proxyAdapter.realAdapter;
    }

    public int getItemCount() {
        return proxyAdapter.realAdapter.getItemCount();
    }

    public interface RecyclerViewScrollCallback {
        void onScrollToBottom();
    }
    public interface OnChildComputeCallback {
        boolean isChildrenNotEnough(LoadMoreRecyclerViewX recyclerView);
    }
    public static class ProxyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LoadMoreRecyclerViewX recyclerViewX;
        private RecyclerView.Adapter<RecyclerView.ViewHolder> realAdapter;

        public ProxyAdapter(LoadMoreRecyclerViewX recyclerViewX) {
            this.recyclerViewX = recyclerViewX;
        }

        private void setRealAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
            realAdapter = adapter;
            notifyDataSetChanged();
        }

        public RecyclerView.Adapter<RecyclerView.ViewHolder> getRealAdapter() {
            return realAdapter;
        }

        private int getRealCount() {
            return realAdapter == null ? 0 : realAdapter.getItemCount();
        }

        @Override
        public int getItemCount() {
            if(!recyclerViewX.autoLoad || (recyclerViewX.flagEnd && !recyclerViewX.showEnd)) {
                return getRealCount();
            }
            return getRealCount() == 0 ? 0 : getRealCount() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if(getRealCount() != getItemCount() && position == getRealCount()) {
                if(!recyclerViewX.flagEnd) {
                    return VIEW_TYPE_LOAD;
                }
                if(recyclerViewX.showEnd) {
                    return VIEW_TYPE_END;
                }
            }
            return realAdapter.getItemViewType(position);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType == VIEW_TYPE_LOAD) {
                return LoadViewHolder.createHolder(parent, recyclerViewX.onLoadViewListener);
            } else if(viewType == VIEW_TYPE_END) {
                return EndViewHolder.createHolder(parent, recyclerViewX.onEndViewListener);
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
                    if(recyclerViewX.onLoadViewListener != null) {
                        recyclerViewX.onLoadViewListener.onLoadViewBind(holder.itemView);
                    }
                    customLabelStr = recyclerViewX.loadTips;
                } else if(holder instanceof EndViewHolder) {
                    if(recyclerViewX.onEndViewListener != null) {
                        recyclerViewX.onEndViewListener.onEndViewBind(holder.itemView);
                    }
                    customLabelStr = recyclerViewX.endTips;
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
}
