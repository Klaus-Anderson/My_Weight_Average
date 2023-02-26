package com.angussoftware.myweightaverage.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    // Create a LiveData with a String
    val currentWeight: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val hasPermissions: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

}