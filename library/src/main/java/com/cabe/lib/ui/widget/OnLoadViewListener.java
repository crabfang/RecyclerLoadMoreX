package com.cabe.lib.ui.widget;

import android.view.View;
import android.view.ViewGroup;

/**
 * 作者：沈建芳 on 2019-05-18 16:55
 */
public interface OnLoadViewListener {
    View onCreateLoadView(ViewGroup parent);
    void onLoadViewBind(View loadView);
}