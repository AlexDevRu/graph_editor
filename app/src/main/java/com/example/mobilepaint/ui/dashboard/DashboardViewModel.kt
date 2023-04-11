package com.example.mobilepaint.ui.dashboard

import android.app.Application
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilepaint.drawing_view.DrawingUtils
import com.example.mobilepaint.models.MyImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val drawingUtils: DrawingUtils,
    app: Application
): ViewModel() {

    private val originalImages = mutableListOf<MyImage>()

    private val _myImages = MutableLiveData<List<MyImage>>()
    val myImages: LiveData<List<MyImage>> = _myImages

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _query = MutableLiveData("")
    val query: LiveData<String> = _query

    private val gson = Gson()

    private val db = Firebase.firestore
    private val images = db.collection("/users/${GoogleSignIn.getLastSignedInAccount(app)?.email}/images")

    init {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            _loading.postValue(true)
            val list1 = async {
                suspendCancellableCoroutine { continuation ->
                    images.get().addOnCompleteListener { result ->
                        if (result.isSuccessful) {
                            val cloudImages = result.result.documents.map {
                                MyImage(
                                    canvasData = drawingUtils.fromJson(it.get("json") as String),
                                    title = it.id,
                                    published = true
                                )
                            }
                            continuation.resume(cloudImages, null)
                        } else {
                            continuation.resume(emptyList<MyImage>(), null)
                        }
                    }
                }
            }
            val list2 = async {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val images = dir.listFiles { file: File -> file.extension == "json" }.orEmpty().toList()
                images.map {
                    MyImage(
                        canvasData = drawingUtils.fromJson(it.readText()),
                        title = it.name,
                        published = false
                    )
                }
            }

            val publishedImages = list1.await()
            val localImages = list2.await()

            originalImages.clear()
            val localImagesMap = hashMapOf<String, MyImage>()
            localImages.forEach {
                localImagesMap[it.title] = it
            }
            publishedImages.forEach {
                if (localImagesMap.contains(it.title)) {
                    localImagesMap[it.title]!!.published = true
                } else {
                    originalImages.add(it)
                    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val file = File(dir, "${it.title}.json")
                    file.createNewFile()
                    file.appendText(it.canvasData.toJson(gson))
                }
            }

            originalImages.addAll(localImagesMap.values)

            _myImages.postValue(originalImages)
            _loading.postValue(false)
        }
    }

    fun updateSearchQuery(query: String?) {
        _query.value = query
        _myImages.value = originalImages.filter { it.title.lowercase().trim().contains(query.orEmpty().lowercase().trim()) }
    }

    fun updateJsonByFileName(fileName: String, published: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val file = File(dir, fileName)
            if (file.exists()) {
                val json = file.readText()
                val canvasData = drawingUtils.fromJson(json)
                val existingImage = originalImages.find { it.title == fileName }
                if (existingImage != null)
                    existingImage.canvasData = canvasData
                else {
                    val newImage = MyImage(canvasData = canvasData, title = fileName, published = published)
                    originalImages.add(newImage)
                }
                withContext(Dispatchers.Main) {
                    updateSearchQuery(query.value)
                }
            }
        }
    }

}