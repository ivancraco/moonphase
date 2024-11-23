package com.ivandev.moonphase.ui.composable

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.ivandev.moonphase.model.LatLng

/*@Composable
fun Location(context: ComponentActivity, permissions: Array<String>, updateLocation: () -> Unit, alertDialog: (show: Boolean) -> Unit) {

    val ssrpr =
        context.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
    val launcherMultiplesPermissions = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            val areGranted = it.values.reduce { acc, next ->
                acc && next
            }
            if (areGranted) {
                //if (isLocationUpdated) return@rememberLauncherForActivityResult
                //starLocationUpdates(locationCallback, fusedLocationClient)
                updateLocation()
                Log.d("pepe", "location granted")
            } else {
                // decirle que acepte los permisos para poder continuar
                alertDialog(true)
                Log.d("pepe", "location no granted")
            }
        })
    if (ssrpr) {
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).also {
            context.startActivity(it)
        }
        // mandarlo pal settings a activar location
        Text(text = "pepe", style = TextStyle(Color.Magenta))
    } else {
        SideEffect {
            launcherMultiplesPermissions.launch(permissions)
        }
    }
    //Text(text = "pepe")
    //return currentLocation
}*/

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
                Log.d("pepe", "location granted")
            } else {
                permissionsDenied()
                Log.d("pepe", "location no granted")
            }
        })
    return launcherMultiplesPermissions
}