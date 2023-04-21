package com.angussoftware.myweightaverage.viewmodel

import android.app.Application
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.*
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    val healthConnectClient: HealthConnectClient
) : ViewModel() {

//    private val permissionController = healthConnectClient?.permissionController
    private var startDateTime = LocalDateTime.now().minusYears(120)
    private var endDateTime = LocalDateTime.now()

    private val _launchPermissionRequest = MutableLiveData<Unit>()
    private val _textViewText = MutableLiveData<String>()
    private val _chartData = MutableLiveData<List<DataEntry>>()

    val launchPermissionRequest: LiveData<Unit>
        get() = _launchPermissionRequest

    val textViewText: LiveData<String>
        get() = _textViewText

    val chartData: LiveData<List<DataEntry>>
        get() = _chartData

    init {
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                _textViewText.postValue(healthConnectClient?.run {
//                    checkPermissionsAndRun()
//                    "HealthConnectClient is not available"
//                } ?: "HealthConnectClient is not available")
//            }
//        }
    }

    fun checkPermissionsAndRun() {
//        viewModelScope.launch {
//            val granted = permissionController?.getGrantedPermissions()
//            if (granted?.containsAll(Companion.requiredPermissions) == true) {
//                // Permissions already granted, proceed with inserting or reading data.
//                fetchAndDisplayCurrentWeight(
//                    LocalDateTime.now().minusDays(365),
//                    LocalDateTime.now()
//                )
//            } else {
//                _launchPermissionRequest.value = Unit
//            }
//        }
    }

    private suspend fun fetchAndDisplayCurrentWeight(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ) {
//        val client = healthConnectClient ?: return
//
//        viewModelScope.launch(Dispatchers.IO) {
//
//            val weightRecords = client.readRecords(
//                ReadRecordsRequest(
//                    WeightRecord::class,
//                    TimeRangeFilter.between(
//                        startDateTime,
//                        endDateTime
//                    )
//                )
//            ).records
//
//            val currentWeight = weightRecords.firstOrNull()?.weight
//            if (currentWeight != null) {
//                withContext(Dispatchers.Main) {
//                    _textViewText.value = "Current Weight: $currentWeight kg"
//                }
//            } else {
//                withContext(Dispatchers.Main) {
//                    _textViewText.value = "No recent weight data available"
//                }
//            }
//
//            val chartDataList = weightRecords.map { record ->
//                ValueDataEntry(
//                    DateTimeFormatter.ofPattern("MM/dd/yyyy").format(
//                        record.time.atZone(ZoneId.systemDefault())
//                    ),
//                    record.weight.inKilograms
//                )
//            }
//
//            withContext(Dispatchers.Main) {
//                _chartData.value = chartDataList
//            }
//        }
    }

    fun setStartDate(year: Int, month: Int, dayOfMonth: Int) {
        startDateTime = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0)
        if (startDateTime > LocalDateTime.now()) {
            startDateTime = LocalDateTime.now().minusDays(1)
        }
        if (startDateTime > endDateTime) {
            endDateTime = LocalDateTime.now()
        }
        viewModelScope.launch {
            fetchAndDisplayCurrentWeight(
                startDateTime,
                endDateTime
            )
        }
    }

    fun setEndDate(year: Int, month: Int, dayOfMonth: Int) {
        endDateTime = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0)
        if (endDateTime > LocalDateTime.now()) {
            endDateTime = LocalDateTime.now()
        }
        if (startDateTime > endDateTime) {
            startDateTime = LocalDateTime.now().minusYears(120)
        }
        viewModelScope.launch {
            fetchAndDisplayCurrentWeight(
                startDateTime,
                endDateTime
            )
        }
    }

    companion object {
        val requiredPermissions = setOf(
            HealthPermission.getReadPermission(WeightRecord::class),
        )
    }
}