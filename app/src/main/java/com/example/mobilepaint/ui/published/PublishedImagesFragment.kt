package com.example.mobilepaint.ui.published

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mobilepaint.databinding.FragmentPublishedImagesBinding
import com.example.mobilepaint.models.MyImage
import com.example.mobilepaint.ui.dashboard.ImagesAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PublishedImagesFragment : Fragment(), ImagesAdapter.Listener {

    private lateinit var binding: FragmentPublishedImagesBinding

    private val viewModel by viewModels<PublishedImagesViewModel>()

    private val imagesAdapter = ImagesAdapter(this, true)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPublishedImagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvMyImages.adapter = imagesAdapter
        observe()
        binding.swipeToRefreshLayout.setOnRefreshListener {
            viewModel.updateImages()
        }
    }

    private fun observe() {
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = it
        }
        viewModel.myImages.observe(viewLifecycleOwner) {
            imagesAdapter.submitList(it)
            binding.rvMyImages.isVisible = it.isNotEmpty()
            binding.llEmptyList.isVisible = it.isEmpty()
        }
    }

    override fun onItemClick(item: MyImage) {
        TODO("Not yet implemented")
    }

    override fun onRemoveItem(item: MyImage) {
        TODO("Not yet implemented")
    }

    override fun onRenameItem(item: MyImage) {
        TODO("Not yet implemented")
    }

    override fun registerContextMenu(view: View) {
        TODO("Not yet implemented")
    }

}