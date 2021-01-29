package io.kailuzhang.github.demo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import io.kailuzhang.github.demo.adapter.FeedAdapter
import io.kailuzhang.github.demo.adapter.GalleryAdapter
import io.kailuzhang.github.demo.databinding.ActivityNoOffsetBinding
import io.kailuzhang.github.tablayoutmediator2.TabLayoutMediator2

class NoOffsetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityNoOffsetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initGallery(binding)

        binding.itemList.adapter = FeedAdapter(this@NoOffsetActivity).also {
            it.items = getItemList()
        }

        val tabList = getTabList()
        TabLayoutMediator2(
            tabLayout = binding.tabLayout,
            recyclerView = binding.itemList,
            tabCount = tabList.size,
            appBarLayout = binding.appbar,
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

    private fun initGallery(binding: ActivityNoOffsetBinding) {
        val galleryList = listOf(R.drawable.a, R.drawable.b)
        val gallerySize = galleryList.size
        binding.vpGallery.apply {
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                @SuppressLint("SetTextI18n")
                override fun onPageSelected(position: Int) {
                    binding.tvIndex.text = "${(position + 1)} / $gallerySize"
                }
            })
            adapter = GalleryAdapter(this@NoOffsetActivity).apply {
                items = galleryList
            }
        }
    }

    private fun getItemList(): List<Item> = mutableListOf(
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
            FeedAdapter.A,
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
            context.startActivity(Intent(context, NoOffsetActivity::class.java))
        }
    }
}