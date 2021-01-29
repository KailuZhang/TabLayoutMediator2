package io.kailuzhang.github.demo.adapter

import android.app.Activity
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import io.kailuzhang.github.demo.databinding.ItemGalleryPhotoBinding

class GalleryAdapter(
    private val host: Activity
) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    var items: List<Int> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemGalleryPhotoBinding.inflate(host.layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
        private val binding: ItemGalleryPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(@DrawableRes resource: Int) {
            binding.galleryPhoto.setImageResource(resource)
        }
    }
}