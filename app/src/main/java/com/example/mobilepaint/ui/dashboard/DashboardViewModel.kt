package com.example.mobilepaint.ui.dashboard

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilepaint.Utils
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
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val drawingUtils: DrawingUtils,
    private val app: Application
): ViewModel() {

    private var originalImages = mutableListOf<MyImage>()

    private val _myImages = MutableLiveData<List<MyImage>>()
    val myImages: LiveData<List<MyImage>> = _myImages

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _query = MutableLiveData("")
    val query: LiveData<String> = _query

    private val gson = Gson()

    private val db = Firebase.firestore

    init {
        updateImages()
    }

    fun updateImages() {
        val imagesCollection = db.collection("/users/${GoogleSignIn.getLastSignedInAccount(app)?.email}/images")
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            _loading.postValue(true)
            val list1 = async {
                suspendCancellableCoroutine { continuation ->
                    imagesCollection.get().addOnCompleteListener { result ->
                        if (result.isSuccessful) {
                            val cloudImages = result.result.documents.map {
                                MyImage(
                                    id = it.id,
                                    canvasData = drawingUtils.fromJson(it.get("json") as String),
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
                val dir = Utils.createAndGetAppDir()
                val images = dir.listFiles { file: File -> file.extension == "json" }.orEmpty().toList()
                images.map {
                    Log.d(TAG, "read image json: ${it.name}")
                    val json = it.readText()
                    MyImage(
                        id = it.nameWithoutExtension,
                        canvasData = drawingUtils.fromJson(json),
                        published = false
                    )
                }
            }

            val publishedImages = list1.await()
            val localImages = list2.await()

            originalImages.clear()
            val localImagesMap = hashMapOf<String, MyImage>()
            localImages.forEach {
                Log.d(TAG, "localImages: ${it.id}")
                localImagesMap[it.id] = it
            }
            publishedImages.forEach {
                if (localImagesMap.contains(it.id)) {
                    Log.d(TAG, "publishedImages: has local ${it.id}")
                    localImagesMap[it.id]!!.published = true
                } else {
                    originalImages.add(it)
                    Log.d(TAG, "publishedImages: no local, download ${it.id}")
                    val dir = Utils.createAndGetAppDir()
                    val file = File(dir, "${it.id}.json")
                    file.createNewFile()
                    file.appendText(it.canvasData.toJson(gson))
                }
            }

            originalImages.addAll(localImagesMap.values)

            withContext(Dispatchers.Main) {
                updateSearchQuery(query.value)
            }

            _loading.postValue(false)
        }
    }

    fun updateSearchQuery(query: String?) {
        _query.value = query
        _myImages.value = originalImages.filter { it.canvasData.title.lowercase().trim().contains(query.orEmpty().lowercase().trim()) }
    }

    fun updateJsonByFileName(oldFileName: String, fileName: String, published: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val dir = Utils.createAndGetAppDir()
            val file = File(dir, "$fileName.json")
            if (file.exists()) {
                val json = file.readText()
                val canvasData = drawingUtils.fromJson(json)

                val existingImage = originalImages.find { it.id == oldFileName }
                if (existingImage != null)
                    originalImages = originalImages.map {
                        if (it == existingImage)
                            it.copy(canvasData = canvasData)
                        else
                            it
                    }.toMutableList()
                else {
                    val newImage = MyImage(canvasData = canvasData, id = fileName, published = published)
                    originalImages.add(newImage)
                }

                withContext(Dispatchers.Main) {
                    updateSearchQuery(query.value)
                }
            }
        }
    }

    fun removeItem(item: MyImage) {
        val email = GoogleSignIn.getLastSignedInAccount(app)?.email
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            val dir = Utils.createAndGetAppDir()
            val file = File(dir, "${item.id}.json")
            file.delete()

            if (!email.isNullOrBlank()) {
                val images = db.collection("/users/$email/images")
                val exists = suspendCancellableCoroutine { continuation ->
                    images.document(item.id).get().addOnCompleteListener {
                        if (it.isSuccessful)
                            continuation.resume(it.result.exists(), null)
                        else
                            continuation.resumeWithException(it.exception ?: Exception())
                    }
                }
                if (exists)
                    suspendCancellableCoroutine { continuation ->
                        images.document(item.id).delete().addOnCompleteListener {
                            if (it.isSuccessful)
                                continuation.resume(Unit, null)
                            else
                                continuation.resumeWithException(it.exception ?: Exception())
                        }
                    }
            }

            originalImages.remove(item)
            withContext(Dispatchers.Main) {
                updateSearchQuery(query.value)
            }
            _loading.postValue(false)
        }
    }

    fun renameItem(item: MyImage, newName: String) {
        val email = GoogleSignIn.getLastSignedInAccount(app)?.email
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)

            val newCanvasData = item.canvasData.copy(title = newName)

            if (!email.isNullOrBlank()) {
                val images = db.collection("/users/$email/images")
                suspendCancellableCoroutine { continuation ->
                    val data = hashMapOf("json" to newCanvasData.toJson(gson))
                    images.document(item.id).set(data).addOnCompleteListener {
                        if (it.isSuccessful)
                            continuation.resume(Unit, null)
                        else
                            continuation.resumeWithException(it.exception ?: Exception())
                    }
                }
            }

            originalImages = originalImages.map {
                if (it.id == item.id)
                    it.copy(canvasData = newCanvasData)
                else
                    it
            }.toMutableList()

            withContext(Dispatchers.Main) {
                updateSearchQuery(query.value)
            }

            _loading.postValue(false)
        }
    }

    companion object {
        private const val TAG = "DashboardViewModel"
    }
}