package com.example.mobilepaint

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _stroke = MutableLiveData(5f)
    val stroke : LiveData<Float> = _stroke

}