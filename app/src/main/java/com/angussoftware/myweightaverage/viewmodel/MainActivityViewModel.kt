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
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val permissionController = HealthConnectClient.getOrCreate(application).permissionController
    private var startDateTime = LocalDateTime.now().minusYears(120)
    private var endDateTime = LocalDateTime.now()

    private val _launchPermissionRequest = MutableLiveData<Unit>()
    private val _textViewText = MutableLiveData<String>()
    private val _startDateText = MutableLiveData<String>()
    private val _endDateText = MutableLiveData<String>()
    private val _chartData = MutableLiveData<List<DataEntry>>()
    private val _errorBoolean = MutableLiveData<Boolean>(false)

    val launchPermissionRequest: LiveData<Unit>
        get() = _launchPermissionRequest

    val textViewText: LiveData<String>
        get() = _textViewText


    val startDateText: LiveData<String>
        get() = _startDateText


    val endDateText: LiveData<String>
        get() = _endDateText

    val chartData: LiveData<List<DataEntry>>
        get() = _chartData

    val errorBoolean: LiveData<Boolean>
        get() = _errorBoolean

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

    private fun initHealthConnectClient(): HealthConnectClient? {
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
                fetchAndDisplayCurrentWeight(
                    startDateTime,
                    endDateTime
                )
            } else {
                _launchPermissionRequest.value = Unit
            }
        }
    }

    private suspend fun fetchAndDisplayCurrentWeight(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ) {
        val client = initHealthConnectClient() ?: return

        viewModelScope.launch(Dispatchers.IO) {

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

            val chartDataList = weightRecords.map { record ->
                ValueDataEntry(
                    DateTimeFormatter.ofPattern("MM/dd/yyyy").format(
                        record.time.atZone(ZoneId.systemDefault())
                    ),
                    record.weight.inKilograms
                )
            }


            withContext(Dispatchers.Main) {
                if (chartDataList.isEmpty()) {
                    _errorBoolean.value = true
                } else {
                    _startDateText.value = chartDataList.first().getValue("x") as String
                    _endDateText.value = chartDataList.last().getValue("x") as String
                    _chartData.value = chartDataList
                }
            }
        }
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

    fun clearErrorBoolean(){
        _errorBoolean.postValue(false)
    }

    companion object {
        val requiredPermissions = setOf(
            HealthPermission.getReadPermission(WeightRecord::class),
        )
    }
}