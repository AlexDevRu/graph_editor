package com.example.mobilepaint.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mobilepaint.R
import com.example.mobilepaint.databinding.FragmentDashboardBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DashboardFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentDashboardBinding

    private val viewModel by viewModels<DashboardViewModel>()

    private val navController by lazy { findNavController() }

    private val imagesAdapter = ImagesAdapter()

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
    }

    private fun observe() {
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = it
        }
        viewModel.myImages.observe(viewLifecycleOwner) {
            imagesAdapter.submitList(it)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnAddCanvas -> navigateToCanvasFragment()
        }
    }

    private fun navigateToCanvasFragment() {
        val action = DashboardFragmentDirections.actionDashboardFragmentToCanvasFragment()
        navController.navigate(action)
    }

}