package com.example.mobilepaint.ui.published

import android.app.Application
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
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PublishedImagesViewModel @Inject constructor(
    private val app: Application,
    private val drawingUtils: DrawingUtils
): ViewModel() {

    private val originalImages = mutableListOf<MyImage>()

    private val _myImages = MutableLiveData<List<MyImage>>()
    val myImages: LiveData<List<MyImage>> = _myImages

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _query = MutableLiveData("")
    val query: LiveData<String> = _query

    private val db = Firebase.firestore

    val gson = Gson()

    init {
        updateImages()
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
                                        val file = java.io.File(app.cacheDir, "${it.id}.json")
                                        file.delete()
                                        file.createNewFile()
                                        val json = it.get("json") as String
                                        file.appendText(json)
                                        MyImage(
                                            id = it.id,
                                            canvasData = drawingUtils.fromJson(json),
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

            originalImages.clear()
            originalImages.addAll(jobs.awaitAll().flatten())

            _myImages.postValue(originalImages)
            _loading.postValue(false)
        }
    }

    fun updateSearchQuery(query: String?) {
        _query.value = query
        _myImages.value = originalImages.filter { it.canvasData.title.lowercase().trim().contains(query.orEmpty().lowercase().trim()) }
    }
}