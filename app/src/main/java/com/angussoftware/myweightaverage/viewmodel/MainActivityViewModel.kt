package com.angussoftware.myweightaverage.viewmodel

import android.app.Application
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.*
import java.util.concurrent.TimeUnit

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
                    "HealthConnectClient is not available"
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
                fetchAndDisplayCurrentWeight()
            } else {
                _launchPermissionRequest.value = Unit
            }
        }
    }

    private suspend fun fetchAndDisplayCurrentWeight() {
        val client = initHealthConnectClient() ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val endDateTime = LocalDateTime.now()
            val startDateTime = endDateTime.minusDays(365)

            val weightRecords = client.readRecords(
                ReadRecordsRequest(
                    WeightRecord::class,
                    TimeRangeFilter.between(
                        startDateTime,
                        endDateTime
                    )
                )
            ).records

            val currentWeight = weightRecords.firstOrNull()?.weight
            if (currentWeight != null) {
                withContext(Dispatchers.Main) {
                    _textViewText.value = "Current Weight: $currentWeight kg"
                }
            } else {
                withContext(Dispatchers.Main) {
                    _textViewText.value = "No recent weight data available"
                }
            }
        }
    }

    companion object {
        val requiredPermissions = setOf(
            HealthPermission.getReadPermission(WeightRecord::class),
        )
    }
}