package com.example.mobilepaint

import android.app.Application
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Environment
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MainViewModel(private val app : Application): AndroidViewModel(app) {

    private val _stroke = MutableLiveData(5f)
    val stroke : LiveData<Float> = _stroke

    private val _loading = MutableLiveData(false)
    val loading : LiveData<Boolean> = _loading

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

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
        }
    }
}