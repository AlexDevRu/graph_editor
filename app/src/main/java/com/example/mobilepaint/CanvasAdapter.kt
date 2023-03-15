package com.example.mobilepaint

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class CanvasAdapter(
    fragment : FragmentActivity,
    private var canvases: List<CanvasData> = listOf()
) : FragmentStateAdapter(fragment) {

    fun addCanvas(canvases : List<CanvasData>) {
        this.canvases = canvases
        notifyItemInserted(canvases.size - 1)
    }

    fun removeCanvas(canvases : List<CanvasData>, position: Int) {
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