# TabLayoutMediator2 -- 实现TabLayout+RecyclerView的锚点定位

## 背景

在ViewPager2发布之后，TabLayout加入了一个非常好用的中间类--``TabLayoutMediator``来实现TabLayout与ViewPager2的绑定与滑动联动效果。今天我们就模仿``TabLayoutMediator``来实现一个TabLayout与RecyclerView的锚点定位功能。效果如下图:

![锚点定位](https://i.loli.net/2020/09/23/63TqSCv5j72tyMb.gif)

## 大致思路

思路是很简单的， 

1. 在每次tab选中的时候, 通过监听TabLayout的``OnTabSelectedListener``, 使RecyclerView滑动到对应位置
2. 在RecyclerView滑动的时候，通过监听RecyclerView的``OnScrollListener``确定tab的选中位置
3. Tab与RecyclerView中的Item的对应方式使用ViewType来实现，让每个tab绑定它所对应的RecyclerView中起始Item与末尾Item的ViewType。

## 代码思路

1. ``TabConfigurationStrategy`` -- TabLayout创建tab的回调接口

   ```kotlin
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
   ```

   其中``onConfigureTab``的返回值即为该Tab对应RecylcerView中起始Item与末尾Item的ViewType的Array

2. ``TabLayoutOnScrollListener`` -- 继承于``RecyclerView.OnScrollListener()``，并持有TabLayout，监听RecylcerView滑动时, 改变TabLayout中Tab的选中状态

   ```kotlin
   private class TabLayoutOnScrollListener(
       tabLayout: TabLayout
   ) : RecyclerView.OnScrollListener() {
       private var previousScrollState = 0
       private var scrollState = 0
       //是否是点击tab滚动
       var tabClickScroll: Boolean = false
       // TabLayout中Tab的选中状态
       var selectedTabPosition: Int = -1
   
       private val tabLayoutRef: WeakReference<TabLayout> = WeakReference(tabLayout)
   
       override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
           super.onScrolled(recyclerView, dx, dy)
           if (tabClickScroll) {
               return
           }
   		//当前可见的第一个Item
           val currItem = recyclerView.findFirstVisibleItemPosition()
           val viewType = recyclerView.adapter?.getItemViewType(currItem) ?: -1
           //根据Item的ViewType与TabLayout中Tab的ViewType的对应情况，选中对应tab
           val tabCount = tabLayoutRef.get()?.tabCount ?: 0
           for (i in 0 until tabCount) {
               val tab = tabLayoutRef.get()?.getTabAt(i)
               val viewTypeArray = tab?.tag as? IntArray
               if (viewTypeArray?.contains(viewType) == true) {
                   val updateText =
                       scrollState != RecyclerView.SCROLL_STATE_SETTLING || previousScrollState == RecyclerView.SCROLL_STATE_DRAGGING
                   val updateIndicator =
                       !(scrollState == RecyclerView.SCROLL_STATE_SETTLING && previousScrollState == RecyclerView.SCROLL_STATE_IDLE)
                   if (selectedTabPosition != i) {
                       selectedTabPosition = i
                       // setScrollPosition不会触发TabLayout的onTabSelected回调
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
           // 区分是手动滚动，还是调用代码滚动
           if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
               tabClickScroll = false
           }
       }
   }
   ```

3. ``RecyclerViewOnTabSelectedListener`` -- 继承``TabLayout.OnTabSelectedListener``， 监听TabLayout中Tab选中时，让RecyclerView滑动到对应位置，根据RecylerView要滑动到的位置此时需要区分3种情况

   1. 在屏幕中第一个可见Item之前，直接调用``recyclerView.scrollToPosition``滑动到对应位置
   2. 在屏幕第一个可见Item和最后一个可见Item之间，使用``view.getTop()``与``recyclerView.scrollBy(0, top)``滑动到对应位置
   3. 在屏幕最后一个可见Item之后，先使用``recyclerView.scrollToPosition``让目标Item滑动到屏幕中可见，再使用``recylerView.post{}``， 走第二种情况，滑动到对应位置

   同时也兼容AppBarLayout，当需要滑动到最上方即position为0，展开AppBar， 其他情况折叠AppBar

   ```kotlin
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
               recyclerView.scrollToPosition(recyclerViewPosition)
           }
           // Target position in firstItem .. lastItem
           recyclerViewPosition <= lastItem -> {
               val top: Int = recyclerView.getChildAt(recyclerViewPosition - firstItem).top
               recyclerView.scrollBy(0, top)
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
   ```

4. ``attach``方法，初始化各种监听，绑定RecyclerView与TabLayout。

## 使用方法

使用起来非常简单，只需要新建一个``TabLayoutMediator2``并调用``attach()``就好了

```kotlin
val tabTextArrayList = arrayListOf("demo1", "demo2", "demo3")
val tabViewTypeArrayList = arrayListof(intArrayOf(1, 2), intArrayOf(7, 8), intArrayOf(9, 11))

TabLayoutMediator2(
    tabLayout = binding.layoutGoodsDetailTop.tabLayout,
    recyclerView = binding.recyclerView,
    tabCount = tabTextArrayList.size,
    appBarLayout = binding.appbar,
    autoRefresh = false,
    tabConfigurationStrategy = object : TabLayoutMediator2.TabConfigurationStrategy {
        override fun onConfigureTab(tab: TabLayout.Tab, position: Int): IntArray {
            tab.setText(tabTextArrayList[position])
            return tabViewTypeArrayList[position]
        }
    }
).apply {
    attach()
}
```

## 最后

``TabLayoutMediator2``是模仿``ViewPager2``与``TabLayout``的绑定类``TabLayoutMediator``实现的，使用简单，建议大家可以去看下原API的实现，如果有什么问题欢迎大家留言。

