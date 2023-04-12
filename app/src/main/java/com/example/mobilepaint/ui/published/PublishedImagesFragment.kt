package com.example.mobilepaint.ui.published

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.mobilepaint.R
import com.example.mobilepaint.databinding.FragmentPublishedImagesBinding
import com.example.mobilepaint.models.MyImage
import com.example.mobilepaint.ui.ImagesFragmentDirections
import com.example.mobilepaint.ui.dashboard.ImagesAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class PublishedImagesFragment : Fragment(), ImagesAdapter.Listener, MenuProvider, SearchView.OnQueryTextListener {

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
        (requireActivity() as MenuHost).addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observe() {
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = it
            binding.swipeToRefreshLayout.isRefreshing = it
        }
        viewModel.myImages.observe(viewLifecycleOwner) {
            imagesAdapter.submitList(it)
            binding.rvMyImages.isVisible = it.isNotEmpty()
            binding.llEmptyList.isVisible = it.isEmpty()
        }
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.maxWidth = Int.MAX_VALUE
        searchView.setQuery(viewModel.query.value, false)
        searchView.setOnQueryTextListener(this)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_dashboard1, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        viewModel.updateSearchQuery(newText)
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onItemClick(item: MyImage) {
        val file = File(requireContext().cacheDir, "${item.title}.json")
        val action = ImagesFragmentDirections.actionImagesFragmentToImageFragment(file.absolutePath)
        findNavController().navigate(action)
    }

}