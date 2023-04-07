package com.example.mobilepaint.ui.dashboard

import android.view.LayoutInflater
import android.view.View.MeasureSpec
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepaint.databinding.ItemMyImageBinding
import com.example.mobilepaint.drawing_view.DrawingView
import com.example.mobilepaint.models.MyImage

class ImagesAdapter : ListAdapter<MyImage, ImagesAdapter.ImageViewHolder>(DIFF_UTIL) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemMyImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ImageViewHolder(
        private val binding: ItemMyImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val drawingView = DrawingView(binding.root.context)

        init {
            binding.preview.isEnabled = false
        }

        fun bind(item: MyImage) {
            drawingView.measure(
                MeasureSpec.makeMeasureSpec(item.canvasData.width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(item.canvasData.height, MeasureSpec.EXACTLY),
            )
            drawingView.layout(0, 0, item.canvasData.width, item.canvasData.height)
            //binding.preview.addShapes(item.canvasData.shapesList, item.canvasData.removedShapesList)
            drawingView.addShapes(item.canvasData.shapesList, item.canvasData.removedShapesList)
            binding.preview.setImageBitmap(drawingView.getBitmap())
            binding.title.text = item.title
        }

    }

}