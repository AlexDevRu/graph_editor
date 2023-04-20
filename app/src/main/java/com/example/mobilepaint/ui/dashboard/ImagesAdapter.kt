package com.example.mobilepaint.ui.dashboard

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobilepaint.R
import com.example.mobilepaint.databinding.ItemMyImageBinding
import com.example.mobilepaint.drawing_view.DrawingView
import com.example.mobilepaint.models.MyImage
import java.io.File

class ImagesAdapter(
    private val listener: Listener,
    private val published: Boolean = false
): ListAdapter<MyImage, ImagesAdapter.ImageViewHolder>(DIFF_UTIL) {

    companion object {
        val DIFF_UTIL = object : ItemCallback<MyImage>() {
            override fun areContentsTheSame(oldItem: MyImage, newItem: MyImage): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: MyImage, newItem: MyImage): Boolean {
                return oldItem.id == newItem.id
            }

            override fun getChangePayload(oldItem: MyImage, newItem: MyImage): Any? {
                return if (oldItem.canvasData.title != newItem.canvasData.title)
                    TITLE_PAYLOAD
                else if (oldItem.published != newItem.published)
                    PUBLISHED_PAYLOAD
                else
                    null
            }
        }

        const val PUBLISHED_PAYLOAD = 1
        const val TITLE_PAYLOAD = 2
    }

    interface Listener {
        fun onItemClick(item: MyImage, imageView: ImageView)
        fun onRenameItem(item: MyImage) = Unit
        fun onRemoveItem(item: MyImage) = Unit
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemMyImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: ImageViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            if (payloads.first() is Int) {
                val type = payloads.first() as Int
                if (type == TITLE_PAYLOAD)
                    holder.bindTitle(getItem(position).canvasData.title)
                else if (type == PUBLISHED_PAYLOAD)
                    holder.bindPublished(getItem(position).published)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    inner class ImageViewHolder(
        private val binding: ItemMyImageBinding
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        private var item: MyImage? = null

        private var popupMenu: PopupMenu? = null

        init {
            binding.preview.isEnabled = false
            binding.root.setOnClickListener(this)
            binding.ivOptions.setOnClickListener(this)
            popupMenu = PopupMenu(binding.root.context, binding.ivOptions)
            popupMenu?.inflate(R.menu.menu_context_image)
            popupMenu?.setOnMenuItemClickListener(this)
            binding.ivCloud.isVisible = !published
            binding.ivOptions.isVisible = !published
        }

        fun bind(item: MyImage) {
            this.item = item

            binding.preview.transitionName = item.id

            bindTitle(item.canvasData.title)
            bindPublished(item.published)

            Glide.with(binding.root)
                .load(item.filePath)
                .into(binding.preview)
        }

        fun bindTitle(title: String) {
            binding.title.text = title
        }

        fun bindPublished(itemPublished: Boolean) {
            binding.ivCloud.isVisible = itemPublished && !published
        }

        override fun onClick(view: View?) {
            when (view) {
                binding.root -> listener.onItemClick(item!!, binding.preview)
                binding.ivOptions -> popupMenu?.show()
            }
        }

        override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
            when (menuItem?.itemId) {
                R.id.rename -> listener.onRenameItem(item!!)
                R.id.remove -> listener.onRemoveItem(item!!)
            }
            return true
        }
    }

}