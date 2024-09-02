package com.hfzq.xfn.framework.widget.recyclerview.interf

interface AdapterItemView<T> {
    fun bindData(data: T, position: Int)
}