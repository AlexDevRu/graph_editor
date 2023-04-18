package com.example.mobilepaint.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.example.mobilepaint.R
import com.example.mobilepaint.databinding.FragmentImagesBinding
import com.example.mobilepaint.ui.canvas.CanvasFragment
import com.example.mobilepaint.ui.dashboard.DashboardFragment
import com.example.mobilepaint.ui.published.PublishedImagesFragment
import com.google.android.material.tabs.TabLayoutMediator

class ImagesFragment : Fragment() {

    private lateinit var binding: FragmentImagesBinding

    private val dashboardFragment = DashboardFragment()

    private val fragments = listOf<Fragment>(
        dashboardFragment,
        PublishedImagesFragment()
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPager.adapter = ViewPagerAdapter(this, fragments)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            if (position == 0)
                tab.setText(R.string.my_images)
            else
                tab.setText(R.string.published_images)
        }.attach()

        setFragmentResultListener(CanvasFragment.KEY) { _, bundle ->
            val fileName = bundle.getString("fileName").orEmpty()
            val oldFileName = bundle.getString("oldFileName")
            val published = bundle.getBoolean("published")
            dashboardFragment.updateJsonByFileName(oldFileName, fileName, published)
        }
    }

}