package com.example.mobilepaint.ui.dashboard

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.mobilepaint.MainViewModel
import com.example.mobilepaint.R
import com.example.mobilepaint.databinding.FragmentDashboardBinding
import com.example.mobilepaint.models.MyImage
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DashboardFragment : Fragment(), View.OnClickListener, ImagesAdapter.Listener, MenuProvider, SearchView.OnQueryTextListener {

    private lateinit var binding: FragmentDashboardBinding

    private val viewModel by viewModels<DashboardViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>()

    private val navController by lazy { findNavController() }

    private val imagesAdapter = ImagesAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAddCanvas.setOnClickListener(this)
        binding.rvMyImages.adapter = imagesAdapter
        observe()
        (requireActivity() as MenuHost).addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observe() {
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = it
            binding.llEmptyList.isVisible = !it && viewModel.myImages.value.isNullOrEmpty()
        }
        viewModel.myImages.observe(viewLifecycleOwner) {
            imagesAdapter.submitList(it)
            binding.llEmptyList.isVisible = it.isEmpty()
        }
        mainViewModel.googleAccount.observe(viewLifecycleOwner) {
            binding.cbShowPublishedImages.isVisible = it != null
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnAddCanvas -> navigateToCanvasFragment()
        }
    }

    private fun navigateToCanvasFragment(fileName: String? = null) {
        val action = DashboardFragmentDirections.actionDashboardFragmentToCanvasFragment(fileName)
        navController.navigate(action)
    }

    override fun onItemClick(item: MyImage) {
        navigateToCanvasFragment(item.title)
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.maxWidth = Int.MAX_VALUE
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
}