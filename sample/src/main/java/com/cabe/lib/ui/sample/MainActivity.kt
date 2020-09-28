package com.cabe.lib.ui.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.cabe.lib.ui.decoration.FastIndexByLetterDecoration
import com.cabe.lib.ui.decoration.FloatButtonDecoration
import com.cabe.lib.ui.decoration.HorizontalScrollBarDecoration
import com.cabe.lib.ui.widget.OnEndViewListener
import com.cabe.lib.ui.widget.OnLoadViewListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

/**
 * 作者：沈建芳 on 2019-05-16 16:01
 */
class MainActivity : AppCompatActivity() {
    private val mMyAdapter = MyAdapter()
    private var curPageIndex = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activity_main_recycler_h.adapter = MyAdapterH().apply {
            setDataList(arrayListOf<String>().apply {
                for(i in 0..5) {
                    add(i.toString())
                }
            })
        }
        activity_main_recycler_h.setAutoLoad(false)
        activity_main_recycler_h.setShowEnd(false)
        activity_main_recycler_h.addItemDecoration(HorizontalScrollBarDecoration())

        activity_main_recycler.adapter = mMyAdapter
        activity_main_recycler.setScrollCallback { activity_main_recycler.postDelayed({ appendData(++curPageIndex) }, 1000) }
        activity_main_recycler.postDelayed({ loadData() }, 1000)
        activity_main_recycler.setOnLoadViewListener(object : OnLoadViewListener {
            override fun onCreateLoadView(parent: ViewGroup): View? {
                return null
            }
            override fun onLoadViewBind(loadView: View) {
                loadView.setBackgroundColor(-0x10000)
                val label = (loadView as ViewGroup).getChildAt(1) as TextView
                label.setTextColor(-0x1)
            }
        })
        activity_main_recycler.setOnEndViewListener(object : OnEndViewListener {
            override fun onCreateEndView(parent: ViewGroup): View? {
                return null
            }
            override fun onEndViewBind(loadView: View) {
                loadView.setBackgroundColor(-0xffff01)
                val label = (loadView as ViewGroup).getChildAt(0) as TextView
                label.setTextColor(-0xff0001)
            }
        })
        activity_main_recycler.addItemDecoration(FloatButtonDecoration())
        activity_main_recycler.addItemDecoration(FastIndexByLetterDecoration {

        })
        activity_main_swipe.setOnRefreshListener {
            activity_main_recycler.postDelayed({
                activity_main_swipe.isRefreshing = false
                loadData()
            }, 1000)
        }
    }

    private fun loadData() {
        curPageIndex = 0
        val dataList: MutableList<String> = ArrayList()
        for (i in 0..9) {
            dataList.add("loadData_$i")
        }
        mMyAdapter.setDataList(dataList)
    }

    private fun appendData(pageIndex: Int) {
        val dataList: MutableList<String> = ArrayList()
        for (i in 0..9) {
            dataList.add("appendData#" + pageIndex + "_" + i)
        }
        mMyAdapter.appendDataList(dataList)
        if (pageIndex > 1) {
            activity_main_recycler!!.setScrollEnd(true)
        }
    }

    private inner class MyAdapter : RecyclerView.Adapter<MyViewHolder>() {
        private var dataList: MutableList<String>? = ArrayList()
        fun setDataList(list: MutableList<String>) {
            dataList = list
            notifyDataSetChanged()
        }

        fun appendDataList(list: List<String>?) {
            val count = itemCount
            if (list != null) {
                dataList!!.addAll(list)
            }
            notifyItemInserted(count)
        }

        private fun getItemData(position: Int): String? {
            return if (position < 0 || position >= itemCount) null else dataList!![position]
        }

        override fun getItemCount(): Int {
            return if (dataList == null) 0 else dataList!!.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout_view, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val data = getItemData(position)
            holder.rowLabel.text = data
        }
    }

    private inner class MyViewHolder(itemView: View) : ViewHolder(itemView) {
        val rowLabel: TextView = itemView.findViewById(R.id.item_layout_view)
    }

    private inner class MyAdapterH : RecyclerView.Adapter<VH>() {
        private var dataList: MutableList<String>? = ArrayList()
        fun setDataList(list: MutableList<String>) {
            dataList = list
            notifyDataSetChanged()
        }

        private fun getItemData(position: Int): String? {
            return if (position < 0 || position >= itemCount) null else dataList!![position]
        }

        override fun getItemCount(): Int {
            return if (dataList == null) 0 else dataList!!.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout_view_h, parent, false)
            return VH(itemView)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val data = getItemData(position)
            holder.rowLabel.text = data
        }
    }
    private inner class VH(itemView: View) : ViewHolder(itemView) {
        val rowLabel: TextView = itemView.findViewById(R.id.item_layout_view)
    }
}