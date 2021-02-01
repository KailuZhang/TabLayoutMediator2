@file:JvmName("RecyclerViewExtensions")
package io.kailuzhang.github.tablayoutmediator2

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * @param offset 顶部偏移量
 */
fun RecyclerView.findFirstVisibleItemPosition(offset: Int = 0): Int {
    return when (val layoutManager = layoutManager) {
        is LinearLayoutManager -> {
            if (offset == 0) {
                layoutManager.findFirstVisibleItemPosition()
            } else {
                val first = layoutManager.findFirstVisibleItemPosition()
                var offsetFirstVisible = first
                for (index in first until layoutManager.itemCount) {
                    layoutManager.findViewByPosition(index)?.let {
                        if (it.top <= offset && it.bottom >= offset) {
                            offsetFirstVisible = index
                            return offsetFirstVisible
                        }
                    }
                }
                offsetFirstVisible
            }
        }
        is StaggeredGridLayoutManager -> {
            if (offset == 0) {
                layoutManager.findFirstVisibleItemPositions(null).firstOrNull() ?: 0
            } else {
                val first = layoutManager.findFirstVisibleItemPositions(null).firstOrNull() ?: 0
                var offsetFirstVisible = first
                for (index in first until layoutManager.itemCount) {
                    layoutManager.findViewByPosition(index)?.let {
                        if (it.top <= offset && it.bottom >= offset) {
                            offsetFirstVisible = index
                            return offsetFirstVisible
                        }
                    }
                }
                offsetFirstVisible
            }
        }
        else -> {
            0
        }
    }
}

fun RecyclerView.findLastVisibleItemPosition(): Int {
    return when (val layoutManager = layoutManager) {
        is LinearLayoutManager -> {
            layoutManager.findLastVisibleItemPosition()
        }
        is StaggeredGridLayoutManager -> {
            layoutManager.findLastVisibleItemPositions(null).firstOrNull() ?: 0
        }
        else -> {
            0
        }
    }
}

fun RecyclerView.scrollToPosition(position: Int?, offset: Int = 0) {
    if (null == position || position < 0) {
        return
    }
    stopScroll()
    when (val layoutManager = layoutManager) {
        is LinearLayoutManager -> {
            layoutManager.scrollToPositionWithOffset(position, offset)
        }
        is StaggeredGridLayoutManager -> {
            layoutManager.scrollToPositionWithOffset(position, offset)
        }
        else -> {
            // only support LinearLayoutManager & StaggeredGridLayoutManager
        }
    }
}