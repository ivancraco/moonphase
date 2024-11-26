package com.ivandev.moonphase.ui.composable

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
fun getLauncherPermissions(permissionsAreGranted: ()->Unit, permissionsDenied: ()-> Unit): ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>> {
    val launcherMultiplesPermissions = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            val areGranted = it.values.reduce { acc, next ->
                acc && next
            }
            if (areGranted) {
                permissionsAreGranted()
            } else {
                permissionsDenied()
            }
        })
    return launcherMultiplesPermissions
}