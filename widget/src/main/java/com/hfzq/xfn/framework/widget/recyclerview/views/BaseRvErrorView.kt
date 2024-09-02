package com.hfzq.xfn.framework.widget.recyclerview.views

import android.content.Context
import android.view.View
import com.hfzq.xfn.framework.widget.recyclerview.interf.AdapterItemView

/**
 * @author xwang
 * 2023/8/11 10:32
 * @description
 **/
class BaseRvErrorView(context: Context) : View(context),
    AdapterItemView<Any> {

    override fun bindData(t: Any, position: Int) {
    }
}