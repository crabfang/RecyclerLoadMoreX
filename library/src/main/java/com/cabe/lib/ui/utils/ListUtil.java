package com.cabe.lib.ui.utils;

import java.util.List;

public class ListUtil {
    private ListUtil() {
        throw new AssertionError();
    }

    public static <V> int getSize(List<V> sourceList) {
        return sourceList == null ? 0 : sourceList.size();
    }

    public static <V> V getItem(List<V> sourceList, int position) {
        int count = getSize(sourceList);
        return position < count && position > -1 ? sourceList.get(position) : null;
    }
}