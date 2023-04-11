package com.example.mobilepaint.ui

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mobilepaint.ui.dashboard.DashboardFragment
import com.example.mobilepaint.ui.published.PublishedImagesFragment

class ViewPagerAdapter(fm: Fragment) : FragmentStateAdapter(fm) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) DashboardFragment() else PublishedImagesFragment()
    }

}