# 类京东商详页——一步实现TabLayout与RecyclerView绑定与锚点定位

## 背景

相信大家现在都很了解``TabLayout``+``ViewPager2``的组合使用，其中``TabLayout``与``ViewPager2``的绑定类``TabLayoutMediator``可以很非常简单的实现``TabLayout``与``ViewPager2``的绑定，这次我们就是按照``TabLayoutMediator``的思路实现``TabLayout``与``RecyclerView``的绑定

## 简介

**TabLayoutMediator2**是``TabLayout``与``RecyclerView``的绑定与锚点定位的仓库，非常适合电商App商品详情页的锚点定位使用，并且支持``CoordinatorLayout+AppBarLayout+TabLayout+RecyclerView``(TabLayout与RecyclerView不重叠的情况)和``TabLayout+RecyclerView``(TabLayout与RecyclerView的情况)。

[Github地址](https://github.com/KailuZhang/TabLayoutMediator2)

### 效果

![sample](https://i.loli.net/2021/02/02/3SCvTVk67gMDhoX.gif)

## 原理

上篇文章已经介绍过：[文章地址](https://juejin.cn/post/6878160381966024718)，这次又增加了对TabLayout与RecyclerView重叠情况的支持，完善了demo

## 使用方法

在gradle中添加

```groovy
dependencies {
  implementation 'io.kailuzhang.github.tablayoutmediator2:tablayoutmediator2:0.1.0'
}
```

可以很简单的调用

```kotlin
// offset就是RecyclerView与TabLayout重叠的高度
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
    		// 返回当前tab对应RecyclerView Adapter的起始ViewType与结束ViewType
        return intArrayOf(startViewType, endViewType)
      }
    }
  }
).attach()
```

### 两种使用情形

1. 不重叠情况（CoordinatorLayout+AppBarLayout+TabLayout+RecyclerView)

   > 这种布局是图库控件与TabLayout都放在AppBarLayout中，RecyclerView在AppBarLayout下方，这种情况下offset = 0

   ![不重叠](https://i.loli.net/2021/02/01/T961rOVE3ziDkoj.gif)

2. 重叠情况(TabLayout+RecyclerView)

   > 这种布局是图库控件是作为RecyclerView的一个Item，TabLayout与RecyclerView重叠，offset = tablayot.height

   ![重叠](https://i.loli.net/2021/02/01/jiOVyG7xmhARvug.gif)
