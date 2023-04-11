package com.example.mobilepaint

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilepaint.drawing_view.GeometryType
import com.example.mobilepaint.models.PenType
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

@HiltViewModel
class MainViewModel @Inject constructor(
    private val app : Application,
    sharedPrefsUtils: SharedPrefsUtils
): ViewModel() {

    private val _stroke = MutableLiveData(5f)
    val stroke : LiveData<Float> = _stroke

    private val _color = MutableLiveData(Color.BLACK)
    val color : LiveData<Int> = _color

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    private val _googleAccount = MutableLiveData(GoogleSignIn.getLastSignedInAccount(app))
    val googleAccount : LiveData<GoogleSignInAccount?> = _googleAccount

    private val _newAccount = MutableSharedFlow<GoogleSignInAccount?>()
    val newAccount = _newAccount.asSharedFlow()

    val gson = Gson()

    private val db = Firebase.firestore

    init {
        _stroke.value = sharedPrefsUtils.strokeWidth
        _color.value = sharedPrefsUtils.color
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun saveAccount(account: GoogleSignInAccount?) {
        _googleAccount.value = account
        if (account != null)
            viewModelScope.launch {
                suspendCancellableCoroutine { continuation ->
                    val data = hashMapOf("email" to "1")
                    db.document("/users/${GoogleSignIn.getLastSignedInAccount(app)?.email}").set(data).addOnCompleteListener {
                        if (it.isSuccessful)
                            continuation.resume(Unit, null)
                        else
                            continuation.resumeWithException(it.exception ?: Exception())
                    }
                }
            }
        viewModelScope.launch { _newAccount.emit(account) }
    }
}