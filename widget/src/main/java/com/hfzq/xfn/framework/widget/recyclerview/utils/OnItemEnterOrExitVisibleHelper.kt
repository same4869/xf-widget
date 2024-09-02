package com.hfzq.xfn.framework.widget.recyclerview.utils


import android.widget.AbsListView
import android.widget.ListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class OnItemEnterOrExitVisibleHelper {
    private var lastStart = -1
    private var lastEnd = 0
    private var listener: OnScrollStatusListener? = null
    fun setOnScrollStatusListener(listener: OnScrollStatusListener?) {
        this.listener = listener
    }

    interface OnScrollStatusListener {
        fun onSelectEnterPosition(postion: Int)
        fun onSelectExitPosition(postion: Int)
    }

    private fun dealScrollEvent(firstVisible: Int, lastVisible: Int) {
        val visibleItemCount = lastVisible - firstVisible
        if (visibleItemCount > 0) {
            if (lastStart == -1) {
                lastStart = firstVisible
                lastEnd = lastVisible
                for (i in lastStart until lastEnd + 1) {
                    if (listener != null) {
                        listener!!.onSelectEnterPosition(i)
                    }
                }
            } else {
                if (firstVisible != lastStart) {
                    if (firstVisible > lastStart) { //向上滑动
                        for (i in lastStart until firstVisible) {
                            if (listener != null) {
                                listener!!.onSelectExitPosition(i)
                            }
                        }
                    } else { //向下滑动
                        for (i in firstVisible until lastStart) {
                            if (listener != null) {
                                listener!!.onSelectEnterPosition(i)
                            }
                        }
                    }
                    lastStart = firstVisible
                }
                //
                if (lastVisible != lastEnd) {
                    if (lastVisible > lastEnd) { //向上滑动
                        for (i in lastEnd until lastVisible) {
                            if (listener != null) {
                                listener!!.onSelectEnterPosition(i + 1)
                            }
                        }
                    } else { //向下滑动
                        for (i in lastVisible until lastEnd) {
                            if (listener != null) {
                                listener!!.onSelectExitPosition(i + 1)
                            }
                        }
                    }
                    lastEnd = lastVisible
                }
            }
        }
    }

    fun setListiewScrollListener(listView: ListView) {
        listView.setOnScrollListener(listiewScrollListener)
    }

    fun setRecyclerScrollListener(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(recyclerScrollListener)
    }

    val listiewScrollListener: AbsListView.OnScrollListener =
        object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}
            override fun onScroll(
                view: AbsListView,
                firstVisibleItem: Int,
                paramVisibleItemCount: Int,
                totalItemCount: Int
            ) {
                val firstVisible = view.firstVisiblePosition
                val lastVisible = view.lastVisiblePosition
                dealScrollEvent(firstVisible, lastVisible)
            }
        }
    val recyclerScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            //RecyclerVew
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (layoutManager != null) {
                    val firstVisible = layoutManager.findFirstVisibleItemPosition()
                    val lastVisible = layoutManager.findLastVisibleItemPosition()
                    var visibleItemCount = lastVisible - firstVisible
                    if (lastVisible == 0) {
                        visibleItemCount = 0
                    }
                    if (visibleItemCount != 0) {
                        dealScrollEvent(firstVisible, lastVisible)
                    }
                }
            }
        }
}