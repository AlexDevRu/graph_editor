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
import com.example.mobilepaint.R
import com.example.mobilepaint.databinding.ItemMyImageBinding
import com.example.mobilepaint.drawing_view.DrawingView
import com.example.mobilepaint.models.MyImage

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
                return oldItem.title == newItem.title
            }
        }
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

    inner class ImageViewHolder(
        private val binding: ItemMyImageBinding
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        private val drawingView = DrawingView(binding.root.context)
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
            drawingView.measure(
                MeasureSpec.makeMeasureSpec(item.canvasData.width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(item.canvasData.height, MeasureSpec.EXACTLY),
            )
            drawingView.layout(0, 0, item.canvasData.width, item.canvasData.height)
            drawingView.addShapes(item.canvasData.shapesList, item.canvasData.removedShapesList)
            binding.preview.setImageBitmap(drawingView.getBitmap())
            binding.title.text = item.title
            binding.ivCloud.isVisible = item.published && !published
            binding.preview.transitionName = item.title
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