package io.kailuzhang.github.demo.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import io.kailuzhang.github.demo.Item
import io.kailuzhang.github.demo.R
import io.kailuzhang.github.demo.databinding.ItemGalleryBinding
import io.kailuzhang.github.demo.databinding.ItemNoramlBinding
import io.kailuzhang.github.demo.utils.UIUtils

class FeedAdapter(private val host: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items: List<Item> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == GALLERY) {
            GalleryViewHolder(host, ItemGalleryBinding.inflate(host.layoutInflater, parent, false))
        } else {
            NormalViewHolder(ItemNoramlBinding.inflate(host.layoutInflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NormalViewHolder -> {
                holder.bind(items[position])
            }
            is GalleryViewHolder -> {
                //
            }
            else -> throw IllegalArgumentException("Not support")
        }
    }

    override fun getItemViewType(position: Int): Int = items[position].viewType

    override fun getItemCount(): Int = items.size

    class NormalViewHolder(
        private val binding: ItemNoramlBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item) {
            binding.text.apply {
                layoutParams.height = UIUtils.dpToPx(item.height)
                text = item.title
                setBackgroundColor(item.backgroundColor)
            }
        }
    }

    class GalleryViewHolder(
        private val host: Activity,
        private val binding: ItemGalleryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            val galleryList = listOf(R.drawable.a, R.drawable.b)
            val gallerySize = galleryList.size
            binding.vpGallery.apply {
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    @SuppressLint("SetTextI18n")
                    override fun onPageSelected(position: Int) {
                        binding.tvIndex.text = "${(position + 1)} / $gallerySize"
                    }
                })
                adapter = GalleryAdapter(host).apply {
                    items = galleryList
                }
            }
        }
    }

    companion object {
        const val A = 0
        const val B = 1
        const val C = 2
        const val D = 3
        const val E = 4
        const val F = 5
        const val G = 6
        const val H = 7
        const val OTHER = 100
        const val GALLERY = 101
    }
}