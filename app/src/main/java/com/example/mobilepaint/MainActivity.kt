package com.example.mobilepaint

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobilepaint.databinding.ActivityMainBinding
import com.example.mobilepaint.drawing_view.ShapesView
import com.example.mobilepaint.drawing_view.shapes.Shape
import com.google.android.material.slider.Slider
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), ShapesView.OnShapeChanged, View.OnClickListener,
    ColorEnvelopeListener, AdapterView.OnItemClickListener,
    Slider.OnChangeListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var menu: Menu

    private val viewModel by viewModels<MainViewModel>()

    private val externalStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.all { it.value }) {
            viewModel.saveImageToExternalStorage(binding.shapesView.getBitmap(), System.currentTimeMillis().toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private val storageActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Environment.isExternalStorageManager()) {
            viewModel.saveImageToExternalStorage(binding.shapesView.getBitmap(), System.currentTimeMillis().toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.shapesView.setOnShapeChangedListener(this)

        binding.etType.setAdapter(PenTypesAdapter(this, viewModel.options))
        binding.etType.onItemClickListener = this

        binding.colorView.setOnClickListener(this)
        binding.strokeWidthSlider.addOnChangeListener(this)

        binding.shapesView.addShapes(viewModel.shapesList, viewModel.removedShapesList)

        observe()
    }

    private fun observe() {
        viewModel.stroke.observe(this) {
            binding.strokeWidthSlider.value = it
            binding.shapesView.strokeWidth = it
        }
        viewModel.color.observe(this) {
            binding.colorView.setBackgroundColor(it)
            binding.shapesView.color = it
        }
        viewModel.loading.observe(this) {
            binding.progressBar.isVisible = it
        }
        viewModel.penType.observe(this) {
            binding.etType.setText(it.text, false)
            binding.shapesView.geometryType = it.geometryType
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.message.collectLatest {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onClick(p0: View?) {
        ColorPickerDialog.Builder(this)
            .setTitle("ColorPicker Dialog")
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton(getString(android.R.string.ok), this)
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        this.menu = menu!!
        menu.findItem(R.id.undo).isEnabled = viewModel.shapesList.isNotEmpty()
        menu.findItem(R.id.redo).isEnabled = viewModel.removedShapesList.isNotEmpty()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.undo -> binding.shapesView.undo()
            R.id.redo -> binding.shapesView.redo()
            R.id.exportImage -> exportImage()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun exportImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e : Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            externalStoragePermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }

    override fun onStackSizesChanged(addedShapesSize: Int, removedShapesSize: Int) {
        menu.findItem(R.id.undo).isEnabled = addedShapesSize > 0
        menu.findItem(R.id.redo).isEnabled = removedShapesSize > 0
    }

    override fun onShapeLongClick(shape: Shape) {
        AlertDialog.Builder(this)
            .setTitle(R.string.remove_shape_question)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                binding.shapesView.removeShape(shape)
            }
            .show()
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

    override fun onStop() {
        super.onStop()
        viewModel.saveShapes(binding.shapesView.getShapesList(), binding.shapesView.getRemovedShapesList())
    }
}