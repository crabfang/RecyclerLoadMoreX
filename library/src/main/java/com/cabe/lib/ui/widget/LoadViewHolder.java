package com.cabe.lib.ui.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cabe.lib.ui.loadmore.R;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 作者：沈建芳 on 2019-05-18 16:53
 */
class LoadViewHolder extends RecyclerView.ViewHolder {
    private LoadViewHolder(View itemView) {
        super(itemView);
    }

    static LoadViewHolder createHolder(ViewGroup parent, OnLoadViewListener listener) {
        View itemView = null;
        if(listener != null) {
            itemView = listener.onCreateLoadView(parent);
        }
        if(itemView == null) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_widget_bottom_loading_view, parent, false);
        }
        return new LoadViewHolder(itemView);
    }
}
