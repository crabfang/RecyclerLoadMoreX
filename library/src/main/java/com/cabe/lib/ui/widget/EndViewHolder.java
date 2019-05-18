package com.cabe.lib.ui.widget;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cabe.lib.ui.loadmore.R;

/**
 * 作者：沈建芳 on 2019-05-18 16:53
 */
class EndViewHolder extends RecyclerView.ViewHolder {
    EndViewHolder(View itemView) {
        super(itemView);
    }

    public static EndViewHolder createHolder(ViewGroup parent, OnEndViewListener listener) {
        View itemView = null;
        if(listener != null) {
            itemView = listener.onCreateEndView(parent);
        }
        if(itemView == null) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_widget_bottom_end_view, parent, false);
        }
        return new EndViewHolder(itemView);
    }
}
