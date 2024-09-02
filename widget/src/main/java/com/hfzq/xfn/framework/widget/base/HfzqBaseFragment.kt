package com.hfzq.xfn.framework.widget.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.util.LinkedList

/**
 * Fragment的基础类
 * 它主要是为了提供必要的规范属性
 * 用于简化一部分重复代码
 * @author xwang
 * @date 2021/03/31
 */
open class HfzqBaseFragment : Fragment() {

    /**
     * Fragment的布局的Id
     * 当Fragment使用一个布局时，
     * 可以直接重写它指定布局的Id，
     * 从而不需要重写创建View的方法
     */
    protected open val layoutId: Int = 0


    /**
     * 是否允许自动激活嵌套Fragment的隐藏状态
     * 如果为true，那么当自身被隐藏时，
     * 也会隐藏当前Fragment中所有激活状态的Fragment
     */
    protected open val nestedHideStatus = true

    /**
     * 被隐藏的Fragment的集合
     * 它主要用于在当前页面被隐藏时，触发子Fragment的隐藏方法，
     * 因为如果当前页面只触发了隐藏，那么子页面不会被感知
     * 如果需要被感知，需要手动触发一次
     */
    private val hiddenChildFragmentList = LinkedList<Fragment>()

    private val pendingTask = LinkedList<Runnable>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (layoutId != 0) {
            return inflater.inflate(layoutId, container, false)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    /**
     * 当参数发生修改时，会触发此方法
     */
    open fun onArgumentsChange() {}

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // 为了兼容ChildFragment场景，如果当前的Fragment被隐藏，那么它的Child也应该被触发隐藏
        // 所以此处检索活跃的Fragment,并且设置隐藏和显示
        // 目前判定方式为：
        // 1. 处于Resumed状态（ViewPager中切换分页会触发
        // 2. 隐藏状态与当前中状态相反（已经显示的需要隐藏，已经隐藏的不一定需要显示
        if (nestedHideStatus && host != null) {
            if (hidden) {
                // 选择性的隐藏Fragment
                childFragmentManager.fragments.forEach { childFragment ->
                    if (childFragment.isResumed) {
                        childFragmentManager.beginTransaction().hide(childFragment).commit()
                        hiddenChildFragmentList.addLast(childFragment)
                    }
                }
            } else {
                // 将所有隐藏的Fragment全都释放
                while (hiddenChildFragmentList.isNotEmpty()) {
                    val childFragment = hiddenChildFragmentList.removeFirst()
                    childFragmentManager.beginTransaction().show(childFragment).commit()
                }
            }
        }
        resumePending()
    }


    override fun onResume() {
        super.onResume()
        resumePending()
    }

    protected fun pendingToResume(task: Runnable) {
        if (isResumed && !isHidden) {
            task.run()
        } else {
            pendingTask.addLast(task)
        }
    }

    protected fun removePending(task: Runnable) {
        pendingTask.remove(task)
    }

    private fun resumePending() {
        if (isResumed && !isHidden) {
            while (pendingTask.isNotEmpty()) {
                pendingTask.removeFirst()?.run()
            }
        }
    }

    protected fun argStr(key: String): String? {
        if (argContainsKey(key)) {
            return arguments?.getString(key)
        }
        return null
    }

    protected fun argBool(key: String): Boolean? {
        if (argContainsKey(key)) {
            return arguments?.getBoolean(key)
        }
        return null
    }

    protected fun argInt(key: String): Int? {
        if (argContainsKey(key)) {
            return arguments?.getInt(key)
        }
        return null
    }

    protected fun argRemove(key: String) {
        arguments?.remove(key)
    }

    protected fun argContainsKey(key: String): Boolean {
        val arg = arguments ?: return false
        return arg.containsKey(key)
    }

    protected val trueResult: (() -> Boolean) = { true }
    protected val falseResult: (() -> Boolean) = { false }

    protected inline fun onceUseArgString(
        key: String,
        callback: (String) -> Unit
    ): (() -> Boolean) {
        return argStr(key).onNotNull {
            argRemove(key)
            callback(it)
        }
    }

    protected inline fun onceUseArgBool(
        key: String,
        callback: (Boolean) -> Unit
    ): (() -> Boolean) {
        return argBool(key).onNotNull {
            argRemove(key)
            callback(it)
        }
    }

    protected inline fun onceUseArgInt(
        key: String,
        callback: (Int) -> Unit
    ): (() -> Boolean) {
        return argInt(key).onNotNull {
            argRemove(key)
            callback(it)
        }
    }

    protected inline fun <reified T : Any> T?.onNotNull(callback: (T) -> Unit): (() -> Boolean) {
        val target = this
        if (target != null) {
            callback(target)
            return trueResult
        }
        return falseResult
    }

    protected inline fun Boolean.ifTrue(callback: () -> Unit): (() -> Boolean) {
        if (this) {
            callback()
            return trueResult
        }
        return falseResult
    }

    protected fun linked(vararg callbackArray: () -> Boolean) {
        callbackArray.forEach {
            if (it.invoke()) {
                return
            }
        }
    }

    protected inline fun <reified T : View> findView(id: Int): T? {
        return view?.findViewById(id)
    }

    /**
     * 检查回调函数的身份
     * 优先级为：父碎片、上下文、传入参数
     */
    protected inline fun <reified T : Any> checkIdentity(
        context: Context? = null,
        callback: (T) -> Unit
    ) {
        checkIdentity<T>(context)?.let(callback)
    }

    protected inline fun <reified T : Any> checkIdentity(c: Context?): T? {
        parentFragment?.let {
            if (it is T) {
                return it
            }
        }
        c?.let {
            if (it is T) {
                return it
            }
        }
        context?.let {
            if (it is T) {
                return it
            }
        }
        return null
    }

    protected inline fun <reified T : Fragment> findFragmentById(id: Int): T? {
        val fragment = childFragmentManager.findFragmentById(id) ?: return null
        if (fragment is T) {
            return fragment
        }
        return null
    }

}