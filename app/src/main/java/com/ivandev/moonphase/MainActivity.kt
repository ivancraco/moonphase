package com.ivandev.moonphase

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.datastore.preferences.core.doublePreferencesKey
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.ivandev.moonphase.model.LatLng
import com.ivandev.moonphase.model.dataStore
import com.ivandev.moonphase.ui.composable.Calendar
import com.ivandev.moonphase.ui.composable.getLauncherPermissions
import com.ivandev.moonphase.ui.theme.DividerColor
import com.ivandev.moonphase.ui.theme.MoonPhaseTheme
import com.ivandev.moonphase.ui.theme.MyBlack
import com.ivandev.moonphase.ui.theme.Purple40
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.compose.material.TopAppBar
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.ivandev.moonphase.ui.composable.CurrentMoon
import com.ivandev.moonphase.ui.theme.MyBlack2
import com.ivandev.moonphase.ui.theme.MyWhite
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var isCalled = false
    private var tabItems = listOf<TabItem>()
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            var currentLocation by remember { mutableStateOf(LatLng(0.0, 0.0)) }
            var locationEnabled by remember { mutableStateOf(isLocationEnabled()) }
            var locationDisableAlertDialog by remember { mutableStateOf(false) }
            var permissionsAreGranted by remember {
                mutableStateOf(permissions.all {
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        it
                    ) == PackageManager.PERMISSION_GRANTED
                })
            }
            var showAlertDialog by remember { mutableStateOf(false) }
            var mTest by remember {
                mutableStateOf(
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            }

            val result = getLauncherPermissions(
                permissionsAreGranted = {
                    if (showAlertDialog) showAlertDialog = false
                    permissionsAreGranted = true
                    locationEnabled = isLocationEnabled()
                }) {
                showAlertDialog = true
                mTest = getShouldShowRequestPermissionRationale()
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    for (location in p0.locations) {
                        Log.d("pepe", "${location.latitude}")
                        Log.d("pepe", "${location.longitude}")
                        currentLocation = LatLng(location.latitude, location.longitude)
                        //saveLocationOnDataStore(location.latitude, location.longitude)
                    }
                }
            }
            MoonPhaseTheme {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    contentColor = MyBlack
                ) {
                    if (showAlertDialog) {
                        if (mTest) {
                            AlertDialog(
                                onDismissRequest = { finish() },
                                title = { Text(text = getString(R.string.required_permissions_message)) },
                                text = { Text(text = getString(R.string.confirm_permissions_to_continue_message)) },
                                confirmButton = {
                                    TextButton(onClick = {
                                        result.launch(permissions)
                                    }) {
                                        Text(text = getString(R.string.confirm_message))
                                    }
                                })
                        } else {
                            val coroutine = rememberCoroutineScope()
                            LaunchedEffect(key1 = Unit, block = {
                                coroutine.launch(Dispatchers.Default) {
                                    var flag = true
                                    while (flag) {
                                        if (permissions.all {
                                                ContextCompat.checkSelfPermission(
                                                    this@MainActivity,
                                                    it
                                                ) == PackageManager.PERMISSION_GRANTED
                                            }) {
                                            flag = false
                                            permissionsAreGranted = true
                                            locationEnabled = isLocationEnabled()
                                            starLocationUpdates()
                                            showAlertDialog = false
                                        }
                                        delay(500)
                                    }
                                }
                            })
                            AlertDialog(
                                onDismissRequest = { finish() },
                                title = { Text(text = getString(R.string.activate_location_title)) },
                                text = { Text(text = getString(R.string.activate_location_message)) },
                                confirmButton = {
                                    TextButton(onClick = {
                                        Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", packageName, null)
                                        ).also {
                                            startActivity(it)
                                        }
                                    }) {
                                        Text(text = getString(R.string.go_to_settings_message))
                                    }
                                })
                        }
                    } else {
                        if (permissionsAreGranted) {
                            if (locationEnabled) {
                                starLocationUpdates()
                                App(currentLocation)
                            } else {
                                if (currentLocation.lat != 0.0 && currentLocation.lng != 0.0) {
                                    App(currentLocation)
                                } else {
                                    if (locationDisableAlertDialog) {
                                        val coroutine = rememberCoroutineScope()
                                        LaunchedEffect(key1 = Unit, block = {
                                            coroutine.launch(Dispatchers.Default) {
                                                var flag = true
                                                while (flag) {
                                                    if (isLocationEnabled()) {
                                                        flag = false
                                                        locationEnabled = true
                                                        locationDisableAlertDialog = false
                                                        //showAlertDialog = false
                                                    }
                                                    delay(500)
                                                }
                                            }
                                        })
                                        AlertDialog(
                                            onDismissRequest = {},
                                            title = { Text(text = getString(R.string.location_disabled_message)) },
                                            text = { Text(text = getString(R.string.activate_location_accurate_message)) },
                                            confirmButton = {}
                                        )
                                    } else {
                                        val pref = dataStore
                                        val coroutineScope = rememberCoroutineScope()
                                        val data = pref.data.map {
                                            LatLng(
                                                it[doublePreferencesKey(getString(R.string.latitude))] ?: 0.0,
                                                it[doublePreferencesKey(getString(R.string.longitude))] ?: 0.0
                                            )
                                        }
                                        LaunchedEffect(key1 = Unit, block = {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                data.collect {
                                                    currentLocation = it
                                                    if (currentLocation.lat == 0.0 && currentLocation.lng == 0.0) {
                                                        locationDisableAlertDialog = true
                                                    }
                                                }
                                            }
                                        })
                                    }
                                }
                            }
                        } else {
                            if (getShouldShowRequestPermissionRationale()) {
                                AlertDialog(
                                    onDismissRequest = { finish() },
                                    title = { Text(text = getString(R.string.required_permissions_message)) },
                                    text = { Text(text = getString(R.string.confirm_permissions_to_continue_message)) },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            result.launch(permissions)
                                        }) {
                                            Text(text = getString(R.string.confirm_message))
                                        }
                                    })
                            } else {
                                SideEffect {
                                    result.launch(permissions)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getShouldShowRequestPermissionRationale(): Boolean {
        return shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    private fun starLocationUpdates() {
        if (isCalled) return
        isCalled = true
        locationCallback.let {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 100
            )
                .setWaitForAccurateLocation(false)
                .setMaxUpdates(1)
                .build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                it,
                Looper.getMainLooper()
            )
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun App(currentLocation: LatLng) {
        if (currentLocation.lat != 0.0 && currentLocation.lng != 0.0) {
            tabItems = listOf(
                TabItem(
                    icon = painterResource(id = R.drawable.moon)
                ),
                TabItem(
                    icon = painterResource(id = R.drawable.calendar)
                )
            )
            var selectedTabIndex by remember {
                mutableIntStateOf(0)
            }
            val state = rememberPagerState {
                tabItems.size
            }
            LaunchedEffect(selectedTabIndex) {
                if (selectedTabIndex != state.currentPage) {
                    state.scrollToPage(selectedTabIndex)
                }
            }
            Column(
                modifier = Modifier
                    .background(color = MyBlack),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopAppBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MyBlack2),
                    backgroundColor = MyBlack
                ) {
                    Text(
                        modifier = Modifier.padding(start = 10.dp),
                        text = "Moon Phase",
                        style = TextStyle(
                            MyWhite,
                            fontSize = 16.sp,
                            fontFamily = FontFamily(
                                Font(
                                    R.font.black_ops_one_regular,
                                    FontWeight.Normal
                                )
                            )
                        )
                    )
                }
                HorizontalPager(
                    state = state, beyondBoundsPageCount = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    userScrollEnabled = false
                ) { index ->
                    when (index) {
                        0 -> CurrentMoon(currentLocation)
                        1 -> Calendar(currentLocation)
                    }
                }
                Divider(
                    color = DividerColor, modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(color = Color.DarkGray)
                )
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MyBlack,
                    contentColor = MyBlack,
                    divider = {},
                    indicator = {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                val path = Path()
                                drawPath(
                                    path = path,
                                    color = MyBlack,
                                    style = Stroke(
                                        width = 20f,
                                        cap = StrokeCap.Square,
                                    )
                                )
                            }
                        )
                    }
                ) {
                    tabItems.forEachIndexed { index, tabItem ->
                        Tab(
                            modifier = Modifier.background(color = MyBlack),
                            selected = index == selectedTabIndex,
                            onClick = { selectedTabIndex = index },
                            selectedContentColor = Color.White,
                            unselectedContentColor = MyBlack,
                            icon = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = tabItem.icon,
                                    contentDescription = null,
                                    tint = if (index == selectedTabIndex) Purple40 else Color.LightGray
                                )
                            })
                    }
                }
            }
        }
    }

    @Preview(showBackground = true, heightDp = 950, widthDp = 350)
    @Composable
    fun GreetingPreview() {
        MoonPhaseTheme {
            App(LatLng(61.38861281532911, 108.01361533580854))
        }
    }

    data class TabItem(
        val icon: Painter
    )

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }
}