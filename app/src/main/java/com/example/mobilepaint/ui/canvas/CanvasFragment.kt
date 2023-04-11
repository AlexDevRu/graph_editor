package com.example.mobilepaint.ui.canvas

import android.Manifest
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.example.mobilepaint.R
import com.example.mobilepaint.databinding.DialogChangeCanvasSizeBinding
import com.example.mobilepaint.databinding.DialogStrokeBinding
import com.example.mobilepaint.databinding.FragmentCanvasBinding
import com.example.mobilepaint.drawing_view.GeometryType
import com.example.mobilepaint.drawing_view.ShapesView
import com.example.mobilepaint.drawing_view.shapes.Shape
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class CanvasFragment : Fragment(), ShapesView.OnShapeChanged, View.OnClickListener,
    ColorEnvelopeListener, MenuProvider {

    private lateinit var binding : FragmentCanvasBinding

    private var menu: Menu? = null

    private val viewModel by viewModels<CanvasViewModel>()

    private val shapesView by lazy { binding.shapesView }

    private val externalStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.all { it.value }) {
            if (viewModel.saveImage == 0)
                viewModel.saveImageToExternalStorage(shapesView.getBitmap())
            else
                viewModel.exportJson(shapesView.color, shapesView.width, shapesView.height, shapesView.shapes, shapesView.removedShapes)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private val storageActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Environment.isExternalStorageManager()) {
            if (viewModel.saveImage == 0)
                viewModel.saveImageToExternalStorage(shapesView.getBitmap())
            else
                viewModel.exportJson(shapesView.color, shapesView.width, shapesView.height, shapesView.shapes, shapesView.removedShapes)
        }
    }

    private val pickPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val uri = result.data!!.data!!
            @Suppress("DEPRECATION") val bitmap = if(Build.VERSION.SDK_INT < 28)
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            else {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            shapesView.addBitmap(bitmap)
        }
    }

    private val pickFileActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val fileName = result.data?.data?.lastPathSegment ?: return@registerForActivityResult
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val json = File(directory, fileName).readText()
            val canvasData = viewModel.addCanvasFromJson(json)
            binding.shapesView.addCanvasData(canvasData)
        }
    }

    private val args by navArgs<CanvasFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCanvasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.colorView.setOnClickListener(this)
        binding.tvStroke.setOnClickListener(this)
        binding.btnPenType.setOnClickListener(this)

        binding.btnCursor.setOnClickListener(this)
        binding.btnSelection.setOnClickListener(this)
        binding.btnPath.setOnClickListener(this)
        binding.btnLine.setOnClickListener(this)
        binding.btnEllipse.setOnClickListener(this)
        binding.btnRect.setOnClickListener(this)
        binding.btnArrow.setOnClickListener(this)
        binding.btnText.setOnClickListener(this)
        binding.btnFill.setOnClickListener(this)
        binding.btnImage.setOnClickListener(this)

        shapesView.setOnShapeChangedListener(this)

        (requireActivity() as MenuHost).addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        if (args.fileName.isNullOrBlank()) {
            binding.zoomLayout.post {
                shapesView.updateLayoutParams<ViewGroup.LayoutParams> {
                    width = binding.zoomLayout.width
                    height = binding.zoomLayout.height
                }
            }
        } else {
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val json = File(directory, args.fileName!!).readText()
            val canvasData = viewModel.addCanvasFromJson(json)
            binding.shapesView.addCanvasData(canvasData)
            binding.zoomLayout.post {
                shapesView.updateLayoutParams<ViewGroup.LayoutParams> {
                    width = canvasData.width
                    height = canvasData.height
                }
            }
        }

        observe()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.colorView -> {
                ColorPickerDialog.Builder(requireContext())
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
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.stroke)
                    .setView(strokeBinding.root)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        viewModel.setStroke(strokeBinding.etType.text?.toString()?.toFloatOrNull() ?: 1f)
                    }
                    .show()
            }
            R.id.btnPenType -> changePenTypesVisibility()
            R.id.btnCursor -> viewModel.setPenType(0).also { changePenTypesVisibility() }
            R.id.btnSelection -> viewModel.setPenType(1).also { changePenTypesVisibility() }
            R.id.btnPath -> viewModel.setPenType(2).also { changePenTypesVisibility() }
            R.id.btnLine -> viewModel.setPenType(3).also { changePenTypesVisibility() }
            R.id.btnEllipse -> viewModel.setPenType(4).also { changePenTypesVisibility() }
            R.id.btnRect -> viewModel.setPenType(5).also { changePenTypesVisibility() }
            R.id.btnArrow -> viewModel.setPenType(6).also { changePenTypesVisibility() }
            R.id.btnText -> viewModel.setPenType(7).also { changePenTypesVisibility() }
            R.id.btnFill -> viewModel.setPenType(8).also { changePenTypesVisibility() }
            R.id.btnImage -> importImage()
        }
    }

    override fun onResume() {
        super.onResume()
        onStackSizesChanged(shapesView.shapes.size, shapesView.removedShapes.size)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        this.menu = menu
        onStackSizesChanged(shapesView.shapes.size, shapesView.removedShapes.size)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.publish -> viewModel.publish(args.fileName, shapesView.width, shapesView.height, shapesView.color, shapesView.shapes)
            R.id.undo -> shapesView.undo()
            R.id.redo -> shapesView.redo()
            R.id.exportImage -> {
                viewModel.saveImage = 0
                exportImage()
            }
            R.id.exportJson -> {
                viewModel.saveImage = 1
                exportImage()
            }
            R.id.openJson -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "file/json"
                pickFileActivityResultLauncher.launch(intent)
            }
            R.id.changeCanvasSize -> {
                val changeCanvasSizeBinding = DialogChangeCanvasSizeBinding.inflate(layoutInflater)
                changeCanvasSizeBinding.etWidth.setText(shapesView.width.toString())
                changeCanvasSizeBinding.etHeight.setText(shapesView.height.toString())
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.change_canvas_size)
                    .setView(changeCanvasSizeBinding.root)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val newWidth = changeCanvasSizeBinding.etWidth.text?.toString()?.toIntOrNull() ?: return@setPositiveButton
                        val newHeight = changeCanvasSizeBinding.etHeight.text?.toString()?.toIntOrNull() ?: return@setPositiveButton
                        shapesView.updateLayoutParams<ViewGroup.LayoutParams> {
                            if (newWidth > 0)
                                width = newWidth
                            if (newHeight > 0)
                                height = newHeight
                        }
                        viewModel.updateCanvasSize(newWidth, newHeight)
                    }
                    .show()
            }
        }
        return true
    }

    private fun importImage() {
        val chooseFile = Intent(Intent.ACTION_PICK)
        chooseFile.type = "image/*"
        pickPhotoLauncher.launch(chooseFile)
    }

    private fun exportImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                if (viewModel.saveImage == 0)
                    viewModel.saveImageToExternalStorage(shapesView.getBitmap())
                else
                    viewModel.exportJson(shapesView.color, shapesView.width, shapesView.height, shapesView.shapes, shapesView.removedShapes)
                return
            }
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e : Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            externalStoragePermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }

    private fun observe() {
        viewModel.stroke.observe(viewLifecycleOwner) {
            binding.tvStroke.text = getString(R.string.stroke_n, it.toInt())
            shapesView.strokeWidth = it
        }
        viewModel.color.observe(viewLifecycleOwner) {
            binding.colorView.setBackgroundColor(it)
            shapesView.color = it
        }
        viewModel.penType.observe(viewLifecycleOwner) {
            shapesView.geometryType = it.geometryType
            val enabled = it.geometryType == GeometryType.ZOOM
            binding.zoomLayout.touchable = enabled
            binding.btnPenType.text = it.text
            binding.btnPenType.setIconResource(it.iconRes)
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.message.collectLatest {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.update.collectLatest {
                    setFragmentResult(KEY, bundleOf("fileName" to it.first, "published" to it.second))
                }
            }
        }
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = it
        }
    }

    private fun changePenTypesVisibility() {
        binding.flPenTypes.isVisible = !binding.flPenTypes.isVisible
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveShapes(shapesView.color, shapesView.shapes, shapesView.removedShapes)
        viewModel.saveCanvasParameters()
    }

    override fun onShapeLongClick(shape: Shape) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.remove_shape_question)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                shapesView.removeShape(shape)
            }
            .show()
    }

    override fun onStackSizesChanged(addedShapesSize: Int, removedShapesSize: Int) {
        menu?.findItem(R.id.undo)?.isEnabled = addedShapesSize > 0
        menu?.findItem(R.id.redo)?.isEnabled = removedShapesSize > 0
    }

    override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
        if (envelope == null) return
        viewModel.setColor(envelope.color)
    }

    companion object {
        const val KEY = "CanvasFragment"
    }

}