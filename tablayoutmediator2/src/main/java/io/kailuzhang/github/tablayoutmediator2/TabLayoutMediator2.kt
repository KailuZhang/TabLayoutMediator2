package io.kailuzhang.github.tablayoutmediator2

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import java.lang.ref.WeakReference

/**
 * A mediator to link a TabLayout with a RecyclerView. The mediator will synchronize the RecyclerView's
 * position with the selected tab when a tab is selected, and the TabLayout's scroll position when
 * the user drags the RecyclerView. TabLayoutMediator will listen to RecyclerView's OnScrollListener
 * to adjust tab when RecyclerView moves. TabLayoutMediator listens to TabLayout's
 * OnTabSelectedListener to adjust RecyclerView when tab moves. TabLayoutMediator listens to RecyclerView's
 * AdapterDataObserver to recreate tab content when dataset changes.
 *
 *
 * Establish the link by creating an instance of this class, make sure the RecyclerView has an
 * adapter and then call [.attach] on it. Instantiating a TabLayoutMediator will only create
 * the mediator object, [.attach] will link the TabLayout and the RecyclerView together. When
 * creating an instance of this class, you must supply an implementation of [ ] in which you set the text of the tab, and/or perform any styling of the
 * tabs that you require. Changing RecyclerView's adapter will require a [.detach] followed by
 * [.attach] call. Changing the RecyclerView or TabLayout will require a new instantiation of
 * TabLayoutMediator.
 */
class TabLayoutMediator2(
    private val tabLayout: TabLayout,
    private val recyclerView: RecyclerView,
    private val tabCount: Int,
    private val appBarLayout: AppBarLayout? = null,
    private val offset: Int = 0,
    private val autoRefresh: Boolean = true,
    private val tabConfigurationStrategy: TabConfigurationStrategy
) {
    private var adapter: RecyclerView.Adapter<*>? = null
    private var attached = false
    private var onScrollListener: TabLayoutOnScrollListener? = null
    private var onTabSelectedListener: OnTabSelectedListener? = null
    private var recyclerViewAdapterObserver: RecyclerView.AdapterDataObserver? = null

    /**
     * A callback interface that must be implemented to set the text and styling of newly created
     * tabs.
     */
    interface TabConfigurationStrategy {
        /**
         * Called to configure the tab for the page at the specified position. Typically calls [ ][TabLayout.Tab.setText], but any form of styling can be applied.
         *
         * @param tab The Tab which should be configured to represent the title of the item at the given
         * position in the data set.
         * @param position The position of the item within the adapter's data set.
         * @return Adapter's first and last view type corresponding to the tab
         */
        fun onConfigureTab(tab: TabLayout.Tab, position: Int): IntArray
    }

    /**
     * Link the TabLayout and the RecyclerView together. Must be called after RecyclerView has an adapter
     * set. To be called on a new instance of TabLayoutMediator or if the RecyclerView's adapter
     * changes.
     *
     * @throws IllegalStateException If the mediator is already attached, or the RecyclerView has no
     * adapter.
     */
    fun attach() {
        check(!attached) { "TabLayoutMediator is already attached" }
        adapter = recyclerView.adapter
        checkNotNull(adapter) { "TabLayoutMediator attached before RecyclerView has an " + "adapter" }
        attached = true

        // Add our custom onScrollListener to the RecyclerView
        onScrollListener = TabLayoutOnScrollListener(tabLayout, offset)
        recyclerView.addOnScrollListener(onScrollListener!!)

        // Now we'll add a tab selected listener to set RecyclerView's current item
        onTabSelectedListener =
            RecyclerViewOnTabSelectedListener(recyclerView) { recyclerViewPosition, tabPosition ->
                moveRecycleViewToPosition(recyclerViewPosition, tabPosition)
            }
        tabLayout.addOnTabSelectedListener(onTabSelectedListener!!)

        // Now we'll populate ourselves from the pager adapter, adding an observer if
        // autoRefresh is enabled
        if (autoRefresh) {
            // Register our observer on the new adapter
            recyclerViewAdapterObserver = RecyclerViewAdapterObserver()
            adapter!!.registerAdapterDataObserver(recyclerViewAdapterObserver!!)
        }
        populateTabsFromPagerAdapter()

        // Now update the scroll position to match the RecyclerView's current item
        refreshCurrentItemTabPosition()
    }

    /**
     * Unlink the TabLayout and the RecyclerView. To be called on a stale TabLayoutMediator2 if a new one
     * is instantiated, to prevent holding on to a view that should be garbage collected. Also to be
     * called before [.attach] when a RecyclerView's adapter is changed.
     */
    fun detach() {
        if (autoRefresh && adapter != null) {
            adapter!!.unregisterAdapterDataObserver(recyclerViewAdapterObserver!!)
            recyclerViewAdapterObserver = null
        }
        tabLayout.removeOnTabSelectedListener(onTabSelectedListener!!)
        recyclerView.removeOnScrollListener(onScrollListener!!)
        onTabSelectedListener = null
        adapter = null
        attached = false
    }

    fun populateTabsFromPagerAdapter() {
        tabLayout.removeAllTabs()
        if (adapter != null) {
            val adapterCount = adapter!!.itemCount
            for (i in 0 until tabCount) {
                val tab = tabLayout.newTab()
                tab.tag = tabConfigurationStrategy.onConfigureTab(tab, i)
                tabLayout.addTab(tab, false)
            }
            // Make sure we reflect the currently set RecyclerView item
            if (adapterCount > 0) {
                refreshCurrentItemTabPosition()
            }
        }
    }

    // Refresh selectedTabPosition according to recyclerView first visible item
    private fun refreshCurrentItemTabPosition() {
        val position = recyclerView.findFirstVisibleItemPosition(offset)
        if (position < 0) return
        val viewType = recyclerView.adapter?.getItemViewType(position) ?: -1
        val tabCount = tabLayout.tabCount
        for (i in 0 until tabCount) {
            val tab = tabLayout.getTabAt(i)
            val viewTypeArray = tab?.tag as IntArray
            if (viewTypeArray.contains(viewType)) {
                if (onScrollListener?.selectedTabPosition != i) {
                    onScrollListener?.selectedTabPosition = i
                    tabLayout.setScrollPosition(
                        i,
                        0f,
                        true
                    )
                }
            }
        }
    }

    private class TabLayoutOnScrollListener(
        tabLayout: TabLayout,
        val offset: Int
    ) : RecyclerView.OnScrollListener() {
        private var previousScrollState = 0
        private var scrollState = 0

        // Is click tab to scroll
        var tabClickScroll: Boolean = false

        // Selected tab position now, because tabLayout.setScrollPosition don't change tabLayout selectedTabPosition
        var selectedTabPosition: Int = -1

        private val tabLayoutRef: WeakReference<TabLayout> = WeakReference(tabLayout)

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (tabClickScroll) {
                return
            }

            val firstVisibleItem =
                recyclerView.findFirstVisibleItemPosition(offset = if (dy == 0) 0 else offset)
            val viewType = recyclerView.adapter?.getItemViewType(firstVisibleItem) ?: -1
            val tabCount = tabLayoutRef.get()?.tabCount ?: 0
            for (i in 0 until tabCount) {
                val tab = tabLayoutRef.get()?.getTabAt(i)
                val viewTypeArray = tab?.tag as? IntArray
                if (viewTypeArray?.contains(viewType) == true) {
                    // Only update the text selection if we're not settling, or we are settling after
                    // being dragged

                    // Only update the text selection if we're not settling, or we are settling after
                    // being dragged
                    val updateText =
                        scrollState != RecyclerView.SCROLL_STATE_SETTLING || previousScrollState == RecyclerView.SCROLL_STATE_DRAGGING
                    // Update the indicator if we're not settling after being idle. This is caused
                    // from a setCurrentItem() call and will be handled by an animation from
                    // onPageSelected() instead.
                    // Update the indicator if we're not settling after being idle. This is caused
                    // from a setCurrentItem() call and will be handled by an animation from
                    // onPageSelected() instead.
                    val updateIndicator =
                        !(scrollState == RecyclerView.SCROLL_STATE_SETTLING && previousScrollState == RecyclerView.SCROLL_STATE_IDLE)
                    if (selectedTabPosition != i) {
                        selectedTabPosition = i
                        tabLayoutRef.get()?.setScrollPosition(
                            i,
                            0f,
                            updateText,
                            updateIndicator
                        )
                        break
                    }
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            previousScrollState = scrollState
            scrollState = newState
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                tabClickScroll = false
            }
        }
    }

    /**
     * A [TabLayout.OnTabSelectedListener] class which contains the necessary calls back to the
     * provided [RecyclerView] so that the tab position is kept in sync.
     */
    private class RecyclerViewOnTabSelectedListener(
        private val recyclerView: RecyclerView,
        private val moveRecyclerViewToPosition: (recyclerViewPosition: Int, tabPosition: Int) -> Unit
    ) : OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            moveRecyclerViewToPosition(tab)
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            moveRecyclerViewToPosition(tab)
        }

        private fun moveRecyclerViewToPosition(tab: TabLayout.Tab) {
            val viewType = (tab.tag as IntArray).first()
            val adapter = recyclerView.adapter
            val itemCount = adapter?.itemCount ?: 0
            for (i in 0 until itemCount) {
                if (adapter?.getItemViewType(i) == viewType) {
                    moveRecyclerViewToPosition.invoke(i, tab.position)
                    break
                }
            }
        }
    }

    private fun moveRecycleViewToPosition(recyclerViewPosition: Int, tabPosition: Int) {
        onScrollListener?.tabClickScroll = true
        onScrollListener?.selectedTabPosition = tabPosition
        val firstItem: Int = recyclerView.findFirstVisibleItemPosition()
        val lastItem: Int = recyclerView.findLastVisibleItemPosition()
        when {
            // Target position before firstItem
            recyclerViewPosition <= firstItem -> {
                recyclerView.scrollToPosition(recyclerViewPosition, offset)
            }
            // Target position in firstItem .. lastItem
            recyclerViewPosition <= lastItem -> {
                val top: Int = recyclerView.getChildAt(recyclerViewPosition - firstItem).top
                recyclerView.scrollBy(0, top - offset)
            }
            // Target position after lastItem
            else -> {
                recyclerView.scrollToPosition(recyclerViewPosition)
                recyclerView.post {
                    moveRecycleViewToPosition(recyclerViewPosition, tabPosition)
                }
            }
        }
        // If have appBar, expand or close it
        if (recyclerViewPosition == 0) {
            appBarLayout?.setExpanded(true, false)
        } else {
            appBarLayout?.setExpanded(false, false)
        }
    }

    private inner class RecyclerViewAdapterObserver : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }
    }
}