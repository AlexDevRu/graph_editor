package com.example.mobilepaint.ui.dashboard

import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.example.mobilepaint.MainViewModel
import com.example.mobilepaint.R
import com.example.mobilepaint.databinding.DialogStrokeBinding
import com.example.mobilepaint.databinding.FragmentDashboardBinding
import com.example.mobilepaint.models.MyImage
import com.example.mobilepaint.ui.ImagesFragmentDirections
import com.example.mobilepaint.ui.canvas.CanvasFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class DashboardFragment : Fragment(), View.OnClickListener, ImagesAdapter.Listener, MenuProvider, SearchView.OnQueryTextListener {

    private lateinit var binding: FragmentDashboardBinding

    private val viewModel by viewModels<DashboardViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>()

    private val navController by lazy { findNavController() }

    private val imagesAdapter = ImagesAdapter(this)

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
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAddCanvas.setOnClickListener(this)
        binding.rvMyImages.adapter = imagesAdapter
        binding.rvMyImages.setHasFixedSize(true)
        observe()
        (requireActivity() as MenuHost).addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        parentFragment?.postponeEnterTransition()
    }

    fun updateJsonByFileName(fileName: String, published: Boolean) {
        viewModel.updateJsonByFileName(fileName, published)
    }

    private fun observe() {
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = it
            binding.llEmptyList.isVisible = !it && viewModel.myImages.value.isNullOrEmpty()
        }
        viewModel.myImages.observe(viewLifecycleOwner) {
            imagesAdapter.submitList(it)
            binding.rvMyImages.isVisible = it.isNotEmpty()
            binding.llEmptyList.isVisible = it.isEmpty()
            (parentFragment?.view?.parent as? ViewGroup)?.doOnPreDraw {
                parentFragment?.startPostponedEnterTransition()
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.newAccount.collectLatest {
                    viewModel.updateImages()
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnAddCanvas -> {
                val action = ImagesFragmentDirections.actionImagesFragmentToCanvasFragment2(null, "")
                navController.navigate(action)
            }
        }
    }

    private fun navigateToCanvasFragment(fileName: String?, imageView: ImageView) {
        val extras = FragmentNavigatorExtras(
            imageView to imageView.transitionName
        )
        val action = ImagesFragmentDirections.actionImagesFragmentToCanvasFragment(fileName, imageView.transitionName)
        navController.navigate(action, extras)
    }

    override fun onItemClick(item: MyImage, imageView: ImageView) {
        navigateToCanvasFragment(item.title, imageView)
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

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        activity?.menuInflater?.inflate(R.menu.menu_context_image, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return false
    }

    override fun onRemoveItem(item: MyImage) {
        viewModel.removeItem(item)
    }

    override fun onRenameItem(item: MyImage) {
        val strokeBinding = DialogStrokeBinding.inflate(layoutInflater)
        strokeBinding.etType.hint = getString(R.string.enter_new_image_name)
        strokeBinding.etType.inputType = InputType.TYPE_CLASS_TEXT
        strokeBinding.etType.setText(item.title)
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.stroke)
            .setView(strokeBinding.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.renameItem(item, strokeBinding.etType.text?.toString().orEmpty())
            }
            .show()
    }
}