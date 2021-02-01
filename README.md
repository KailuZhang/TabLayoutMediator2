# Tablayout与RecyclerView锚点定位库——TabLayoutMediator2版本发布

## 背景

之前已经写了一篇[博客](https://juejin.cn/post/6878160381966024718)详细介绍了TabLayoutMediator2的具体实现，并把代码放到了github上，但是还存在两点问题

1. 没有写Sample
2. 只支持``TabLayout``与``RecyclerView``不重叠的情况，即``RecyclerView``在``TabLayout``的下方

## 新版本

版本地址：[TabLayoutMediator2](https://github.com/KailuZhang/TabLayoutMediator2)

这次解决了以上的问题

1. 完成了事例应用
2. 支持了``TabLayout``与``RecyclerView``重叠的情况
3. 发布到了jcenter仓库中

## 使用方法

在gradle中添加

```groovy
dependencies {
		implementation 'io.kailuzhang.github.tablayoutmediator2:tablayoutmediator2:0.1.0'
}
```

```kotlin
TabLayoutMediator2(
  tabLayout = binding.tabLayout,
  recyclerView = binding.itemList,
  tabCount = tabList.size,
  appBarLayout = binding.appbar,
  offset = 0，
  autoRefresh = false,
  tabConfigurationStrategy = object : TabLayoutMediator2.TabConfigurationStrategy {
    override fun onConfigureTab(tab: TabLayout.Tab, position: Int): IntArray {
      tabList[position].apply {
        tab.text = title
        return intArrayOf(startViewType, endViewType)
      }
    }
  }
).attach()
```

## 效果

1. 不重叠情况（CoordinatorLayout+AppBarLayout+TabLayout+RecyclerView)，这种布局RecyclerView就在TabLayout下方

   ![不重叠](https://i.loli.net/2021/02/01/T961rOVE3ziDkoj.gif)

   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
       xmlns:app="http://schemas.android.com/apk/res-auto"
       xmlns:tools="http://schemas.android.com/tools"
       android:id="@+id/coordinator_layout"
       android:layout_width="match_parent"
       android:layout_height="match_parent">
   
       <com.google.android.material.appbar.AppBarLayout
           android:id="@+id/appbar"
           android:layout_width="match_parent"
           android:layout_height="wrap_content">
   
           <com.google.android.material.appbar.CollapsingToolbarLayout
               android:id="@+id/toolbar_layout"
               android:layout_width="match_parent"
               android:layout_height="wrap_content"
               app:toolbarId="@id/toolbar">
               
   						<!--  图库  -->
               <androidx.viewpager2.widget.ViewPager2
                   android:id="@+id/vp_gallery"
                   android:layout_width="match_parent"
                   android:layout_height="370dp"
                   app:layout_collapseMode="parallax"
                   tools:background="@android:color/darker_gray" />
   
               <androidx.appcompat.widget.Toolbar
                   android:id="@+id/toolbar"
                   android:layout_width="match_parent"
                   android:layout_height="44dp">
   
                   <com.google.android.material.tabs.TabLayout
                       android:id="@+id/tab_layout"
                       android:layout_width="match_parent"
                       android:layout_height="44dp" />
   
               </androidx.appcompat.widget.Toolbar>
   
           </com.google.android.material.appbar.CollapsingToolbarLayout>
   
       </com.google.android.material.appbar.AppBarLayout>
   
       <androidx.recyclerview.widget.RecyclerView
           android:id="@+id/item_list"
           android:layout_width="match_parent"
           android:layout_height="match_parent"
           android:orientation="vertical"
           app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"
           app:layout_behavior="@string/appbar_scrolling_view_behavior" />
   
   </androidx.coordinatorlayout.widget.CoordinatorLayout>
   ```

2. 重叠情况(TabLayout+RecyclerView)

   ![重叠](https://i.loli.net/2021/02/01/jiOVyG7xmhARvug.gif)

   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
       xmlns:app="http://schemas.android.com/apk/res-auto"
       xmlns:tools="http://schemas.android.com/tools"
       android:layout_width="match_parent"
       android:layout_height="match_parent">
   
       <androidx.recyclerview.widget.RecyclerView
           android:id="@+id/item_list"
           android:layout_width="match_parent"
           android:layout_height="match_parent"
           android:orientation="vertical"
           app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" />
   
       <com.google.android.material.tabs.TabLayout
           android:id="@+id/tab_layout"
           android:layout_width="match_parent"
           android:layout_height="44dp"
           android:alpha="0"
           android:background="@android:color/white"
           app:layout_constraintTop_toTopOf="parent"
           app:tabContentStart="0dp"
           app:tabGravity="fill"
           app:tabIndicatorColor="@android:color/black"
           app:tabIndicatorFullWidth="false"
           app:tabIndicatorGravity="bottom"
           app:tabMode="fixed"
           app:tabPaddingEnd="0dp"
           app:tabPaddingStart="0dp"
           app:tabTextAppearance="@style/TabStyle"
           tools:background="@android:color/darker_gray" />
   
   </androidx.constraintlayout.widget.ConstraintLayout>
   ```

## 最后

如果大家觉得这个库好用的话，欢迎大家star，多谢！

另外最近也在找工作，希望有大佬可以帮忙内推，多谢！

邮箱： zhkl2014@gmail.com

微信：zhangkailu