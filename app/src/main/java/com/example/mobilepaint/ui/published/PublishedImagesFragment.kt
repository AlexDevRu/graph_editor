package com.example.mobilepaint.ui.published

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.example.mobilepaint.R
import com.example.mobilepaint.databinding.FragmentPublishedImagesBinding
import com.example.mobilepaint.models.MyImage
import com.example.mobilepaint.ui.ImagesFragment
import com.example.mobilepaint.ui.ImagesFragmentDirections
import com.example.mobilepaint.ui.dashboard.ImagesAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class PublishedImagesFragment : Fragment(), ImagesAdapter.Listener, MenuProvider, SearchView.OnQueryTextListener {

    private lateinit var binding: FragmentPublishedImagesBinding

    private val viewModel by viewModels<PublishedImagesViewModel>()

    private val imagesAdapter = ImagesAdapter(this, true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementReturnTransition = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
    }

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
        binding.rvMyImages.setHasFixedSize(true)

        binding.swipeToRefreshLayout.setOnRefreshListener {
            viewModel.updateImages()
        }
        (requireActivity() as MenuHost).addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)


        observe()
        parentFragment?.postponeEnterTransition()
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
            (parentFragment?.view?.parent as? ViewGroup)?.doOnPreDraw {
                parentFragment?.startPostponedEnterTransition()
            }
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

    override fun onItemClick(item: MyImage, imageView: ImageView) {
        val extras = FragmentNavigatorExtras(
            imageView to imageView.transitionName
        )
        val file = File(requireContext().cacheDir, "${item.title}.json")

        val action = ImagesFragmentDirections.actionImagesFragmentToImageFragment(file.absolutePath, item.title)
        findNavController().navigate(action, extras)
    }

}