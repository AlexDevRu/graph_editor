package com.example.mobilepaint

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mobilepaint.drawing_view.DrawingUtils
import com.example.mobilepaint.drawing_view.GeometryType
import com.example.mobilepaint.models.PenType
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val app : Application,
    private val sharedPrefsUtils: SharedPrefsUtils,
    private val drawingUtils: DrawingUtils
): ViewModel() {

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

    private val _stroke = MutableLiveData(5f)
    val stroke : LiveData<Float> = _stroke

    private val _color = MutableLiveData(Color.BLACK)
    val color : LiveData<Int> = _color

    private val _loading = MutableLiveData(false)
    val loading : LiveData<Boolean> = _loading

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    private val _googleAccount = MutableLiveData(GoogleSignIn.getLastSignedInAccount(app))
    val googleAccount : LiveData<GoogleSignInAccount?> = _googleAccount

    val gson = Gson()

    init {
        _stroke.value = sharedPrefsUtils.strokeWidth
        _color.value = sharedPrefsUtils.color
    }

    fun saveAccount(account: GoogleSignInAccount?) {
        _googleAccount.value = account
    }
}