package com.angussoftware.myweightaverage.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.WeightRecord
import com.angussoftware.myweightaverage.R
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    val PERMISSIONS =
        setOf(
            HealthPermission.getReadPermission(WeightRecord::class),
        )

    // Create the permissions launcher.
    val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()

    val requestPermissions =
        registerForActivityResult(requestPermissionActivityContract) { granted ->
            if (granted.containsAll(PERMISSIONS)) {
                // Permissions successfully granted
                findViewById<TextView>(R.id.textView).text = "Permissions successfully granted"

            } else {
                // Lack of required permissions
                findViewById<TextView>(R.id.textView).text = "Lack of required permissions"
            }
        }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (HealthConnectClient.isProviderAvailable(this)) {
            val healthConnectClient = HealthConnectClient.getOrCreate(this)
            findViewById<TextView>(R.id.textView).text = healthConnectClient.toString()

            // build a set of permissions for required data types

            GlobalScope.launch {
                checkPermissionsAndRun(healthConnectClient)
            }
        } else {
            // ...
        }
    }

    suspend fun checkPermissionsAndRun(healthConnectClient: HealthConnectClient) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(PERMISSIONS)) {
            // Permissions already granted, proceed with inserting or reading data.
            runOnUiThread {
                findViewById<TextView>(R.id.textView).text = "Permissions already granted, proceed with inserting or reading data."
            }
        } else {
            requestPermissions.launch(PERMISSIONS)
        }
    }

}