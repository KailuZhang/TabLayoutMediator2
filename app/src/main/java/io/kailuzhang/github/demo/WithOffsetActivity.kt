package io.kailuzhang.github.demo

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import io.kailuzhang.github.demo.adapter.FeedAdapter
import io.kailuzhang.github.demo.databinding.ActivityWithOffsetBinding
import io.kailuzhang.github.demo.utils.UIUtils
import io.kailuzhang.github.tablayoutmediator2.TabLayoutMediator2
import kotlin.math.abs

class WithOffsetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityWithOffsetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.itemList.adapter = FeedAdapter(this@WithOffsetActivity).also {
            it.items = getItemList()
        }
        binding.itemList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val firstVisible = ((recyclerView.layoutManager) as? LinearLayoutManager)?.findFirstVisibleItemPosition()
                val top = recyclerView.getChildAt(0)?.top ?: 0
                binding.tabLayout.alpha = if (firstVisible == 0) abs(top) / 800f else 1f
            }
        })

        val tabList = getTabList()
        TabLayoutMediator2(
            tabLayout = binding.tabLayout,
            recyclerView = binding.itemList,
            tabCount = tabList.size,
            offset = UIUtils.dpToPx(40f),
            tabConfigurationStrategy = object : TabLayoutMediator2.TabConfigurationStrategy {
                override fun onConfigureTab(tab: TabLayout.Tab, position: Int): IntArray {
                    tabList[position].apply {
                        tab.text = title
                        return intArrayOf(startViewType, endViewType)
                    }
                }
            }
        ).attach()
    }

    private fun getItemList(): List<Item> = mutableListOf(
        Item(FeedAdapter.GALLERY),
        Item(FeedAdapter.A, Color.BLUE, "A", 100f),
        Item(FeedAdapter.B, Color.YELLOW, "B", 200f),
        Item(FeedAdapter.C, Color.WHITE, "C", 100f),
        Item(FeedAdapter.D, Color.GREEN, "D", 200f),
        Item(FeedAdapter.E, Color.LTGRAY, "E", 100f),
        Item(FeedAdapter.F, Color.BLUE, "F", 100f),
        Item(FeedAdapter.G, Color.WHITE, "G", 100f),
        Item(FeedAdapter.H, Color.GREEN, "H", 100f)
    ).apply {
        repeat(20) {
            add(Item(FeedAdapter.OTHER, Color.BLUE, "OTHER", 40f))
        }
    }

    private fun getTabList(): List<TabData> = listOf(
        TabData(
            "Gallery",
            FeedAdapter.GALLERY,
            FeedAdapter.C
        ),
        TabData(
            "D",
            FeedAdapter.D,
            FeedAdapter.F
        ),
        TabData(
            "G",
            FeedAdapter.G,
            FeedAdapter.H
        ),
        TabData(
            "OTHER",
            FeedAdapter.OTHER,
            FeedAdapter.OTHER
        )
    )

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, WithOffsetActivity::class.java))
        }
    }
}