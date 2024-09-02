package com.hfzq.xfn.framework.widget.recyclerview.interf

/**
 * @author xwang
 * 2023/8/10 15:15
 * @description 数据到view映射协议
 **/
interface AdapterUIMappingProtocol<T> {
    companion object {
        const val ERROR_ITEM_TYPE = -1
    }

    //数据 ——> Type
    fun getItemType(data: T): Int

    // Type -> View
    fun createItem(type: Int): AdapterItemView<*>?
}