package com.example.mobilepaint

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.example.mobilepaint.drawing_view.GeometryType
import com.example.mobilepaint.drawing_view.shapes.Shape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MainViewModel(private val app : Application): AndroidViewModel(app) {

    val options = listOf(
        PenType(app.getString(R.string.cursor), R.drawable.ic_hand, GeometryType.HAND),
        PenType(app.getString(R.string.path), R.drawable.ic_curve, GeometryType.PATH),
        PenType(app.getString(R.string.line), R.drawable.ic_line, GeometryType.LINE),
        PenType(app.getString(R.string.ellipse), R.drawable.ic_ellipse, GeometryType.ELLIPSE),
        PenType(app.getString(R.string.rectangle), R.drawable.ic_rectangle, GeometryType.RECT),
        PenType(app.getString(R.string.arrow), R.drawable.ic_arrow, GeometryType.ARROW),
        PenType(app.getString(R.string.fill), R.drawable.ic_paint, GeometryType.PAINT),
    )

    data class ShapeWrapper(
        val shapesList : LinkedList<Shape> = LinkedList(),
        val removedShapesList : LinkedList<Shape> = LinkedList()
    )

    private val _stroke = MutableLiveData(5f)
    val stroke : LiveData<Float> = _stroke

    private val _penType = MutableLiveData(options.first())
    val penType : LiveData<PenType> = _penType

    private val _color = MutableLiveData(Color.BLACK)
    val color : LiveData<Int> = _color

    private val _loading = MutableLiveData(false)
    val loading : LiveData<Boolean> = _loading

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    private val _openFile = MutableSharedFlow<Uri>()
    val openFile = _openFile.asSharedFlow()

    val canvases = mutableListOf<ShapeWrapper>()

    fun addCanvas() {
        canvases.add(ShapeWrapper())
    }

    fun removeCanvas(position: Int) {
        canvases.removeAt(position)
    }

    init {
        canvases.add(ShapeWrapper())
    }

    fun setStroke(stroke : Float) {
        _stroke.value = stroke
    }

    fun setColor(color : Int) {
        _color.value = color
    }

    fun setPenType(position : Int) {
        _penType.value = options[position]
    }

    fun saveShapes(position: Int, shapesList : LinkedList<Shape>, removedShapesList : LinkedList<Shape>) {
        canvases[position] = ShapeWrapper(shapesList, removedShapesList)
    }

    fun saveImageToExternalStorage(image : Bitmap, filename: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val file = File(directory, "$filename.jpeg")
            val outputStream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.fd.sync()
            outputStream.close()
            MediaScannerConnection.scanFile(app, arrayOf(file.absolutePath), null, null)
            _loading.postValue(false)
            _message.emit("Saved")
            _openFile.emit(file.toUri())
        }
    }
}