package com.example.mobilepaint.ui.published

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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class PublishedImagesViewModel @Inject constructor(
    private val app: Application,
    private val drawingUtils: DrawingUtils
): ViewModel() {

    private var originalImages = mutableListOf<MyImage>()

    private val _myImages = MutableLiveData<List<MyImage>>()
    val myImages: LiveData<List<MyImage>> = _myImages

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _query = MutableLiveData("")
    val query: LiveData<String> = _query

    private val db = Firebase.firestore

    val gson = Gson()

    companion object {
        private const val TAG = "PublishedImagesViewMode"
    }

    init {
        updateImages()
    }

    private fun observeChanges() {
        viewModelScope.launch {
            callbackFlow {
                val listener = db.collection("users").addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    if (value != null) {
                        for (doc in value)
                            if (doc.id != GoogleSignIn.getLastSignedInAccount(app)?.email)
                                viewModelScope.launch { send(doc) }
                    }
                }

                awaitClose { listener.remove() }
            }.flatMapMerge {
                callbackFlow {
                    val listener = db.collection("users/${it.id}/images").addSnapshotListener { value, error ->
                        if (error != null) {
                            Log.w(TAG, "Listen failed.", error)
                            return@addSnapshotListener
                        }

                        if (value != null) {
                            val images = value.documents.map {
                                val file = File(app.cacheDir, "${it.id}.json")
                                file.delete()
                                file.createNewFile()
                                val json = it.get("json") as String
                                file.writeText(json)
                                val canvasData = drawingUtils.fromJson(json)
                                MyImage(
                                    id = it.id,
                                    filePath = Utils.saveBitmap(it.id, app, canvasData),
                                    canvasData = canvasData,
                                    published = true
                                )
                            }
                            images.forEach { cloudImage ->
                                val index = originalImages.indexOfFirst { it.id == cloudImage.id }
                                if (index >= 0) {
                                    originalImages[index] = cloudImage
                                } else {
                                    originalImages.add(cloudImage)
                                }
                            }
                            _myImages.postValue(originalImages.map { it })
                            viewModelScope.launch { send(images) }
                        }
                    }

                    awaitClose { listener.remove() }
                }
            }.collect()
        }
    }

    fun updateImages() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            val emails = suspendCancellableCoroutine { continuation ->
                db.collection("users").get().addOnCompleteListener {
                    if (it.isSuccessful)
                        continuation.resume(it.result.documents, null)
                    else
                        continuation.resumeWithException(it.exception ?: Exception())
                }
            }

            val jobs = mutableListOf<Deferred<List<MyImage>>>()
            emails.forEach {
                if (it.id != GoogleSignIn.getLastSignedInAccount(app)?.email) {
                    val job = async {
                        suspendCancellableCoroutine { continuation ->
                            db.collection("users/${it.id}/images").get().addOnCompleteListener { result ->
                                if (result.isSuccessful) {
                                    val cloudImages = result.result.documents.map {
                                        val file = File(app.cacheDir, "${it.id}.json")
                                        file.delete()
                                        file.createNewFile()
                                        val json = it.get("json") as String
                                        file.appendText(json)
                                        val canvasData = drawingUtils.fromJson(json)
                                        MyImage(
                                            id = it.id,
                                            filePath = Utils.saveBitmap(it.id, app, canvasData),
                                            canvasData = canvasData,
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
                    jobs.add(job)
                }
            }

            originalImages = jobs.awaitAll().flatten().toMutableList()

            _myImages.postValue(originalImages)
            _loading.postValue(false)

            observeChanges()
        }
    }

    fun updateSearchQuery(query: String?) {
        _query.value = query
        _myImages.value = originalImages.filter { it.canvasData.title.lowercase().trim().contains(query.orEmpty().lowercase().trim()) }
    }
}