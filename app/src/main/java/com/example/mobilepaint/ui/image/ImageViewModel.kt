package com.example.mobilepaint.ui.image

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilepaint.Utils
import com.example.mobilepaint.drawing_view.DrawingUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(
    private val app: Application,
    private val drawingUtils: DrawingUtils
): ViewModel() {

    private lateinit var bitmap: Bitmap

    private val _loading = MutableLiveData(false)
    val loading : LiveData<Boolean> = _loading

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    fun getBitmapFromFile(filePath: String) : Bitmap {
        val file = File(filePath)
        val json = file.readText()
        val canvasData = drawingUtils.fromJson(json)
        bitmap = drawingUtils.getBitmap(canvasData)
        return bitmap
    }

    fun saveImageToExternalStorage() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            try {
                Utils.saveBitmap(app, bitmap, System.currentTimeMillis().toString())
                _message.emit("Saved")
            } catch (e: Exception) {
                _message.emit(e.message.orEmpty())
            } finally {
                _loading.postValue(false)
            }
        }
    }

}