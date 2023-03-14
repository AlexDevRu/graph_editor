package com.example.mobilepaint

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mobilepaint.databinding.FragmentCanvasBinding
import com.example.mobilepaint.drawing_view.ShapesView
import com.example.mobilepaint.drawing_view.shapes.Shape

class CanvasFragment : Fragment(), ShapesView.OnShapeChanged {

    private lateinit var binding : FragmentCanvasBinding

    private var key = 0
    private var menu: Menu? = null

    private val viewModel by lazy {
        ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

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

    private val pickPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val uri = result.data!!.data!!
            val bitmap = if(Build.VERSION.SDK_INT < 28)
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            else {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            binding.shapesView.addBitmap(bitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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
        key = arguments?.getInt(KEY) ?: 0

        binding.shapesView.setOnShapeChangedListener(this)

        binding.shapesView.addShapes(viewModel.canvases[key].shapesList, viewModel.canvases[key].removedShapesList)

        observe()
    }

    override fun onResume() {
        super.onResume()
        onStackSizesChanged(binding.shapesView.shapes.size, binding.shapesView.removedShapes.size)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu
        onStackSizesChanged(binding.shapesView.shapes.size, binding.shapesView.removedShapes.size)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.undo -> binding.shapesView.undo()
            R.id.redo -> binding.shapesView.redo()
            R.id.exportImage -> exportImage()
            R.id.importImage -> importImage()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun importImage() {
        val chooseFile = Intent(Intent.ACTION_PICK)
        chooseFile.type = "image/*"
        pickPhotoLauncher.launch(chooseFile)
    }

    private fun exportImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e : Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            externalStoragePermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }

    private fun observe() {
        viewModel.stroke.observe(viewLifecycleOwner) {
            binding.shapesView.strokeWidth = it
        }
        viewModel.color.observe(viewLifecycleOwner) {
            binding.shapesView.color = it
        }
        viewModel.penType.observe(viewLifecycleOwner) {
            binding.shapesView.geometryType = it.geometryType
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveShapes(key, binding.shapesView.shapes, binding.shapesView.removedShapes)
    }

    override fun onShapeLongClick(shape: Shape) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.remove_shape_question)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                binding.shapesView.removeShape(shape)
            }
            .show()
    }

    override fun onStackSizesChanged(addedShapesSize: Int, removedShapesSize: Int) {
        menu?.findItem(R.id.undo)?.isEnabled = addedShapesSize > 0
        menu?.findItem(R.id.redo)?.isEnabled = removedShapesSize > 0
    }

    companion object {
        private const val KEY = "KEY"

        fun createInstance(position : Int) : CanvasFragment {
            val fragment = CanvasFragment()
            fragment.arguments = bundleOf(KEY to position)
            return fragment
        }
    }

}