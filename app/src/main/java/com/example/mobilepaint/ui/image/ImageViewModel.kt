package com.example.mobilepaint.ui.image

import android.graphics.Bitmap
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilepaint.drawing_view.DrawingUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(
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
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val file = File(directory, "${System.currentTimeMillis()}.jpeg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            _loading.postValue(false)
            _message.emit("Saved")
        }
    }

}