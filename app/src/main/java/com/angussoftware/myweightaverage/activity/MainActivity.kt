package com.angussoftware.myweightaverage.activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.angussoftware.myweightaverage.R
import com.angussoftware.myweightaverage.databinding.ActivityMainBinding
import com.angussoftware.myweightaverage.viewmodel.MainActivityViewModel
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.charts.Cartesian
import com.anychart.enums.TooltipPositionMode

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var anyChartView: AnyChartView
    private lateinit var chart: Cartesian

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupDateButtons(binding.startDateButton, binding.endDateButton)

        setupChartView(binding.anyChartView)

        viewModel.launchPermissionRequest.observe(this) {
            val permissions = MainActivityViewModel.requiredPermissions.toTypedArray()
            requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
        }

        viewModel.chartData.observe(this) { dataEntries ->
            chart.removeAllSeries()
            chart.line(dataEntries)
        }

        viewModel.errorBoolean.observe(this) {
            if(it){
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("There was an error.")
                    .setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }
    }

    private fun setupDateButtons(startDateButton: Button, endDateButton: Button) {
        mapOf(startDateButton to DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            viewModel.setStartDate(year, month, dayOfMonth)
        }, endDateButton to DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            viewModel.setEndDate(year, month, dayOfMonth)
        }).forEach { (button, onDateSetLister) ->
            button.setOnClickListener {
                DatePickerDialog(this).apply {
                    setOnDateSetListener(onDateSetLister)
                }.show()
            }
        }

    }

    private fun setupChartView(anyChartView: AnyChartView) {
        this.anyChartView = anyChartView
        chart = AnyChart.line()

        chart.apply {
            animation(false)
            title(getString(R.string.weight_over_time))
            yAxis(0).title(getString(R.string.weight_kg))
            xAxis(0).title(getString(R.string.Date)).labels().rotation(-75)
        }

        anyChartView.setChart(chart)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            viewModel.checkPermissionsAndRun()
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
    }
}

