package com.example.mobilepaint

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobilepaint.databinding.ActivityMainBinding
import com.example.mobilepaint.databinding.DialogStrokeBinding
import com.example.mobilepaint.databinding.ViewTabBinding
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), View.OnClickListener,
    ColorEnvelopeListener, AdapterView.OnItemClickListener,
    Slider.OnChangeListener, TabLayout.OnTabSelectedListener {

    private lateinit var binding: ActivityMainBinding

    private val canvasAdapter by lazy {
        CanvasAdapter(this, viewModel.canvases)
    }

    private val viewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private var firstCreation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etType.setAdapter(PenTypesAdapter(this, viewModel.options))
        binding.etType.onItemClickListener = this

        binding.colorView.setOnClickListener(this)
        binding.tvStroke.setOnClickListener(this)

        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.adapter = canvasAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val tabBinding = ViewTabBinding.inflate(layoutInflater)
            tabBinding.btnRemove.setOnClickListener {
                viewModel.removeCanvas(position)
                binding.tabLayout.removeTab(tab)
                canvasAdapter.removeCanvas(viewModel.canvases, position)
                changeRemoveButtonVisibility()
            }
            tabBinding.text.text = getString(R.string.canvas_n, position + 1)
            tab.customView = tabBinding.root
        }.attach()

        observe()

        firstCreation = savedInstanceState == null
    }

    override fun onStart() {
        super.onStart()
        if (firstCreation) {
            binding.viewPager.post {
                viewModel.setFirstCanvas(binding.viewPager.width, binding.viewPager.height)
                canvasAdapter.setCanvases(viewModel.canvases)
            }
        }
    }

    private fun changeRemoveButtonVisibility() {
        val visible = binding.tabLayout.tabCount > 1
        for (i in 0 until binding.tabLayout.tabCount)
            binding.tabLayout.getTabAt(i)?.customView?.let {
                val tabBinding = ViewTabBinding.bind(it)
                tabBinding.btnRemove.isVisible = visible
            }
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        val view = tab?.customView ?: return
        val tabBinding = ViewTabBinding.bind(view)
        tabBinding.text.isSelected = true
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {}

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        val view = tab?.customView ?: return
        val tabBinding = ViewTabBinding.bind(view)
        tabBinding.text.isSelected = false
    }

    private fun observe() {
        viewModel.stroke.observe(this) {
            binding.tvStroke.text = getString(R.string.stroke_n, it.toInt())
        }
        viewModel.color.observe(this) {
            binding.colorView.setBackgroundColor(it)
        }
        viewModel.loading.observe(this) {
            binding.progressBar.isVisible = it
        }
        viewModel.penType.observe(this) {
            binding.etType.setText(it.text, false)
            binding.tlType.setStartIconDrawable(it.iconRes)
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.message.collectLatest {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
        /*lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.openFile.collectLatest {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(it, "image/")
                    startActivity(intent)
                }
            }
        }*/
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.colorView -> {
                ColorPickerDialog.Builder(this)
                    .setTitle("ColorPicker Dialog")
                    .setPreferenceName("MyColorPickerDialog")
                    .setPositiveButton(getString(android.R.string.ok), this)
                    .attachAlphaSlideBar(true)
                    .attachBrightnessSlideBar(true)
                    .setBottomSpace(12)
                    .show()
            }
            R.id.tvStroke -> {
                val strokeBinding = DialogStrokeBinding.inflate(layoutInflater)
                strokeBinding.etType.setText(viewModel.stroke.value?.toInt()?.toString())
                AlertDialog.Builder(this)
                    .setTitle(R.string.stroke)
                    .setView(strokeBinding.root)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        viewModel.setStroke(strokeBinding.etType.text?.toString()?.toFloatOrNull() ?: 1f)
                    }
                    .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.newCanvas -> {
                viewModel.addCanvas(binding.viewPager.width, binding.viewPager.height)
                val tab = binding.tabLayout.newTab()
                binding.tabLayout.addTab(tab)
                canvasAdapter.addCanvas(viewModel.canvases)
                changeRemoveButtonVisibility()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
        if (envelope == null) return
        viewModel.setColor(envelope.color)
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        viewModel.setPenType(position)
    }

    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
        viewModel.setStroke(value)
    }
}