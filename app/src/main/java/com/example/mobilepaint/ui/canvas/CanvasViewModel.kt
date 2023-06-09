package com.example.mobilepaint.ui.canvas

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.*
import com.example.mobilepaint.R
import com.example.mobilepaint.SharedPrefsUtils
import com.example.mobilepaint.Utils
import com.example.mobilepaint.drawing_view.DrawingUtils
import com.example.mobilepaint.drawing_view.GeometryType
import com.example.mobilepaint.drawing_view.shapes.Shape
import com.example.mobilepaint.models.CanvasData
import com.example.mobilepaint.models.PenType
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

@HiltViewModel
class CanvasViewModel @Inject constructor(
    private val app: Application,
    private val drawingUtils: DrawingUtils,
    private val sharedPrefsUtils: SharedPrefsUtils,
    savedStateHandle: SavedStateHandle
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

    var canvas = CanvasData(title = "", bg = Color.WHITE)

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

    private val _update = MutableSharedFlow<Pair<String, Boolean>>()
    val update = _update.asSharedFlow()

    var saveImage = 0

    private val db = Firebase.firestore
    private val images = db.collection("/users/${GoogleSignIn.getLastSignedInAccount(app)?.email}/images")

    private val published = savedStateHandle.get<Boolean>("published") ?: false

    private val handler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch { _message.emit(throwable.message.orEmpty()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun publish(fileName: String?, width: Int, height: Int, color: Int, shapesList : List<Shape>) {
        canvas.bg = color
        canvas.shapesList = shapesList
        canvas.width = width
        canvas.height = height
        canvas.title = fileName ?: Utils.generateFileName()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loading.postValue(true)
                val json = canvas.toJson(gson)

                val newFileName = Utils.createOrOverwriteJson(app, json, canvas.title)

                val data = hashMapOf("json" to json)
                suspendCancellableCoroutine { continuation ->
                    images.document(newFileName).set(data).addOnCompleteListener {
                        if (it.isSuccessful) {
                            continuation.resume(Unit, null)
                        } else {
                            continuation.resumeWithException(it.exception ?: Exception())
                        }
                    }
                }
                _message.emit(app.getString(R.string.published))
                _update.emit(newFileName to true)
            } catch (e: Exception) {
                _message.emit(e.message.orEmpty())
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun saveJson(fileName: String, width: Int, height: Int, color: Int, shapesList : List<Shape>) {
        canvas.bg = color
        canvas.shapesList = shapesList
        canvas.width = width
        canvas.height = height
        viewModelScope.launch(Dispatchers.IO + handler) {
            try {
                _loading.postValue(true)
                val json = canvas.toJson(gson)

                Utils.createOrOverwriteJson(app, json, fileName)

                _message.emit(app.getString(R.string.saved))
                _update.emit(fileName to published)
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun saveImageToExternalStorage(image : Bitmap) {
        viewModelScope.launch(Dispatchers.IO + handler) {
            _loading.postValue(true)
            try {
                Utils.saveBitmap(app, image)
                _message.emit(app.getString(R.string.saved))
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun exportJson(color: Int, width: Int, height: Int, shapesList : List<Shape>, removedShapesList : List<Shape>) {
        canvas.bg = color
        canvas.shapesList = shapesList
        canvas.removedShapesList = removedShapesList
        canvas.width = width
        canvas.height = height
        canvas.title = Utils.generateFileName()

        viewModelScope.launch(Dispatchers.IO + handler) {
            try {
                _loading.postValue(true)
                val json = canvas.toJson(gson)
                val fileName = Utils.createOrOverwriteJson(app, json, canvas.title)

                _message.emit(app.getString(R.string.saved))
                _update.emit(fileName to false)
            } finally {
                _loading.postValue(false)
            }
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