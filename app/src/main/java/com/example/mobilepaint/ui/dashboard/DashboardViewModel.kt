package com.example.mobilepaint.ui.dashboard

import android.app.Application
import android.graphics.Paint
import android.os.Environment
import android.view.View.MeasureSpec
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilepaint.Utils
import com.example.mobilepaint.drawing_view.DrawingUtils
import com.example.mobilepaint.drawing_view.DrawingView
import com.example.mobilepaint.models.MyImage
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileFilter
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val drawingUtils: DrawingUtils,
    private val app: Application
): ViewModel() {

    private val _myImages = MutableLiveData<List<MyImage>>()
    val myImages: LiveData<List<MyImage>> = _myImages

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    init {

        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val images = dir.listFiles { file: File -> file.extension == "json" }.orEmpty().toList()
            val myImages = images.map {
                MyImage(
                    canvasData = drawingUtils.fromJson(it.readText()),
                    title = it.name
                )
            }
            _myImages.postValue(myImages)
            _loading.postValue(false)
        }
    }

}