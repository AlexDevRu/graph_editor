package com.example.mobilepaint.ui.canvas

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilepaint.R
import com.example.mobilepaint.SharedPrefsUtils
import com.example.mobilepaint.drawing_view.DrawingUtils
import com.example.mobilepaint.drawing_view.GeometryType
import com.example.mobilepaint.drawing_view.shapes.Shape
import com.example.mobilepaint.models.CanvasData
import com.example.mobilepaint.models.PenType
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CanvasViewModel @Inject constructor(
    private val app: Application,
    private val drawingUtils: DrawingUtils,
    private val sharedPrefsUtils: SharedPrefsUtils
) : ViewModel() {

    private val options = listOf(
        PenType(app.getString(R.string.cursor), R.drawable.ic_cursor, GeometryType.ZOOM),
        PenType(app.getString(R.string.selection), R.drawable.ic_hand, GeometryType.HAND),
        PenType(app.getString(R.string.path), R.drawable.ic_curve, GeometryType.PATH),
        PenType(app.getString(R.string.line), R.drawable.ic_line, GeometryType.LINE),
        PenType(app.getString(R.string.ellipse), R.drawable.ic_ellipse, GeometryType.ELLIPSE),
        PenType(app.getString(R.string.rectangle), R.drawable.ic_rectangle, GeometryType.RECT),
        PenType(app.getString(R.string.arrow), R.drawable.ic_arrow, GeometryType.ARROW),
        PenType(app.getString(R.string.text), R.drawable.ic_text, GeometryType.TEXT),
        PenType(app.getString(R.string.fill), R.drawable.ic_paint, GeometryType.PAINT),
    )

    private val gson = Gson()

    var canvas = CanvasData(bg = Color.WHITE)

    private val _loading = MutableLiveData(false)
    val loading : LiveData<Boolean> = _loading

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    private val _stroke = MutableLiveData(5f)
    val stroke : LiveData<Float> = _stroke

    private val _penType = MutableLiveData(options.first())
    val penType : LiveData<PenType> = _penType

    private val _color = MutableLiveData(Color.BLACK)
    val color : LiveData<Int> = _color

    var saveImage = 0

    fun setFirstCanvas(minWidth: Int, minHeight: Int) {
        canvas.width = minWidth
        canvas.height = minHeight
    }

    fun saveImageToExternalStorage(image : Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val file = File(directory, "$${System.currentTimeMillis()}.jpeg")
            val outputStream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.fd.sync()
            outputStream.close()
            MediaScannerConnection.scanFile(app, arrayOf(file.absolutePath), null, null)
            _loading.postValue(false)
            _message.emit("Saved")
        }
    }

    fun exportJson(color: Int, shapesList : List<Shape>, removedShapesList : List<Shape>) {
        canvas.bg = color
        canvas.shapesList = shapesList
        canvas.removedShapesList = removedShapesList
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            val json = canvas.toJson(gson)
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.ENGLISH)
            val fileName = dateFormat.format(Date())
            val file = File(dir, "$fileName.json")
            file.appendText(json)
            file.createNewFile()
            _loading.postValue(false)
            _message.emit("Saved")
        }
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

    fun updateCanvasSize(width: Int, height: Int) {
        if (width > 0)
            canvas.width = width
        if (height > 0)
            canvas.height = height
    }

    fun saveShapes(color: Int, shapesList : List<Shape>, removedShapesList : List<Shape>) {
        canvas.bg = color
        canvas.shapesList = shapesList
        canvas.removedShapesList = removedShapesList
    }

    fun addCanvasFromJson(json: String) : CanvasData {
        canvas = drawingUtils.fromJson(json)
        return canvas
    }

    fun saveCanvasParameters() {
        sharedPrefsUtils.color = color.value!!
        sharedPrefsUtils.strokeWidth = stroke.value!!
    }
}