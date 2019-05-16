### Integration
``` xml

dependencies {
    implementation "com.cabe.lib.ui:RecyclerLoadMore:<last_version>"
}

``` 

### Usage


``` xml

		<com.cabe.lib.ui.widget.LoadMoreRecyclerView
			android:id="@+id/activity_main_recycler"
			android:layout_width="match_parent"
			android:layout_height="match_parent"
			tools:listitem="@layout/item_layout_view"
			app:spanCount="3"
			app:layoutManager="android.support.v7.widget.GridLayoutManager"/>

```

``` java

        LoadMoreRecyclerView recyclerView = findViewById(R.id.activity_main_recycler);
        recyclerView.setScrollCallback(RecyclerViewScrollCallback);
    

```