package com.angussoftware.myweightaverage.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.WeightRecord
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.angussoftware.myweightaverage.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val permissionController = HealthConnectClient.getOrCreate(application).permissionController
    private val _launchPermissionRequest = MutableLiveData<Unit>()
    private val _textViewText = MutableLiveData<String>()
    val launchPermissionRequest: LiveData<Unit>
        get() = _launchPermissionRequest

    val textViewText: LiveData<String>
        get() = _textViewText

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _textViewText.postValue(initHealthConnectClient()?.run {
                    checkPermissionsAndRun()
                    toString()
                } ?: "HealthConnectClient is not available")
            }
        }
    }

    private suspend fun initHealthConnectClient(): HealthConnectClient? {
        return if (HealthConnectClient.isProviderAvailable(getApplication())) {
            HealthConnectClient.getOrCreate(getApplication())
        } else {
            null
        }
    }

    fun checkPermissionsAndRun() {
        viewModelScope.launch {
            val granted = permissionController.getGrantedPermissions()
            if (granted.containsAll(Companion.requiredPermissions)) {
                // Permissions already granted, proceed with inserting or reading data.
                // Do something here
            } else {
                _launchPermissionRequest.value = Unit
            }
        }
    }

    companion object {
        val requiredPermissions = setOf(
            HealthPermission.getReadPermission(WeightRecord::class),
        )
    }
}

