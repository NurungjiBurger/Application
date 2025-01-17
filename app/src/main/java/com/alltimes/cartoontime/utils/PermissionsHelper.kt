package com.alltimes.cartoontime.utils

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat

object PermissionsHelper {
    private val requiredPermissions =
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.UWB_RANGING,
            Manifest.permission.BLUETOOTH_SCAN
        )

    fun hasAllPermissions(activity: ComponentActivity): Boolean {
        return requiredPermissions.all {
            ActivityCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestPermissions(activity: ComponentActivity) {
        ActivityCompat.requestPermissions(activity, requiredPermissions, 0)
    }

    fun allPermissionsGranted(grantResults: IntArray): Boolean {
        return grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
    }
}
