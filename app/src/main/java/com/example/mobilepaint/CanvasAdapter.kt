package com.example.mobilepaint

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class CanvasAdapter(
    fragment : FragmentActivity,
    private var canvases: List<MainViewModel.ShapeWrapper> = listOf()
) : FragmentStateAdapter(fragment) {

    fun setCanvases(canvases : List<MainViewModel.ShapeWrapper>) {
        this.canvases = canvases
        notifyDataSetChanged()
    }

    fun addCanvas(canvases : List<MainViewModel.ShapeWrapper>) {
        this.canvases = canvases
        notifyItemInserted(canvases.size - 1)
    }

    fun removeCanvas(canvases : List<MainViewModel.ShapeWrapper>, position: Int) {
        this.canvases = canvases
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int {
        return canvases.size
    }

    override fun createFragment(position: Int): Fragment {
        return CanvasFragment.createInstance(position)
    }

}