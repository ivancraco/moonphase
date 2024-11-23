package com.ivandev.moonphase

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
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
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.glide.rememberGlidePainter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.ivandev.moonphase.model.GetMoonImage
import com.ivandev.moonphase.model.LatLng
import com.ivandev.moonphase.model.PhaseModel
import com.ivandev.moonphase.model.dataStore
import com.ivandev.moonphase.ui.composable.Calendar
import com.ivandev.moonphase.ui.composable.getLauncherPermissions
import com.ivandev.moonphase.ui.composable.optionsCache
import com.ivandev.moonphase.ui.theme.DividerColor
import com.ivandev.moonphase.ui.theme.MoonPhaseTheme
import com.ivandev.moonphase.ui.theme.MyBlack
import com.ivandev.moonphase.ui.theme.Purple40
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.shredzone.commons.suncalc.MoonIllumination
import org.shredzone.commons.suncalc.MoonPhase
import org.shredzone.commons.suncalc.MoonPosition
import org.shredzone.commons.suncalc.MoonTimes
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.roundToInt
import androidx.compose.material.TopAppBar
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.updateAll
import com.ivandev.moonphase.data.cipher.Encode
import com.ivandev.moonphase.model.CurrentMoonWidget
import com.ivandev.moonphase.ui.theme.MyBlack2
import com.ivandev.moonphase.ui.theme.MyWhite
import com.ivandev.moonphase.ui.theme.NextPhases
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var locationRequired = false
    private var isCalled = false

    private var tabItems = listOf<TabItem>()
    var aux = ""

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
                    //starLocationUpdates()
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
                        saveLocationOnDataStore(location.latitude, location.longitude)
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
                                        //showAlertDialog = false
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
                                        //showAlertDialog = false
                                    }) {
                                        Text(text = getString(R.string.go_to_settings_message))
                                    }
                                })
                        }
                    } else {
                        if (permissionsAreGranted) {
                            Log.d("pepe", "A3")
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
                                //App(currentLocation = currentLocation)
                            }
                        } else {
                            Log.d("pepe", "B1")
                            if (getShouldShowRequestPermissionRationale()) {
                                AlertDialog(
                                    onDismissRequest = { finish() },
                                    title = { Text(text = getString(R.string.required_permissions_message)) },
                                    text = { Text(text = getString(R.string.confirm_permissions_to_continue_message)) },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            result.launch(permissions)
                                            //showAlertDialog = false
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

    override fun onResume() {
        super.onResume()
        if (locationRequired) {
            // starLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        /* locationCallback?.let {
             fusedLocationClient?.removeLocationUpdates(it)
         }*/
    }

    @SuppressLint("MissingPermission")
    private fun starLocationUpdates() {
        Log.d("pepe", "A1")
        if (isCalled) return
        Log.d("pepe", "A2")
        isCalled = true
        locationCallback.let {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 100
            )
                .setWaitForAccurateLocation(false)
                //.setMinUpdateIntervalMillis(3000)
                //.setMaxUpdateDelayMillis(100)
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
        /*val launcherMultiplesPermissions = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = {
                try {
                    val areGranted = it.values.reduce { acc, next ->
                        acc && next
                    }
                    if (areGranted) {
                        locationRequired = true
                        starLocationUpdates()
                        Log.d("pepe", "location granted")
                    } else {
                        Log.d("pepe", "location no granted")
                    }
                } catch (e: Exception) {
                    Log.d("pepe", "B: ${e.localizedMessage}")
                }
            })
        if (permissions.all {
                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }) {
            SideEffect {
                launcherMultiplesPermissions.launch(permissions)
            }
        } else {*/
        //starLocationUpdates()
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
            /*LaunchedEffect(state.currentPage) {
                if (selectedTabIndex != state.currentPage) {
                    delay(250)
                    selectedTabIndex = state.currentPage
                }
            }*/
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
                /*Text(
                    modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                    text = "${currentLocation.lat}/${currentLocation.lng}",
                    style = TextStyle(color = Color.White, fontSize = 12.sp)
                )*/
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

    @Composable
    fun CurrentMoon(currentLocation: LatLng) {
        val calendar = Calendar.getInstance()
        val cDay = calendar.get(Calendar.DAY_OF_MONTH)
        val cMonth = calendar.get(Calendar.MONTH)
        val cYear = calendar.get(Calendar.YEAR)
        var str = "${cDay}/${cMonth + 1}/${cYear}"
        val nowLong = calendar.time
        val newMoon =
            MoonPhase.compute().phase(MoonPhase.Phase.NEW_MOON).on(nowLong)
                .execute().time
        val fullMoon =
            MoonPhase.compute().phase(MoonPhase.Phase.FULL_MOON).on(nowLong)
                .execute().time
        val formatter = DateFormat.getDateInstance()
        //formatter.timeZone = TimeZone.getTimeZone("America/New_York")
        str = formatter.format(calendar.time)
        val moonPosition =
            MoonPosition.compute().on(nowLong).at(currentLocation.lat, currentLocation.lng)
                .execute()
        val angle =
            moonPosition.parallacticAngle
        var test = ""
        val param = MoonIllumination.compute().on(nowLong).execute()
        val azimuth = moonPosition.azimuth
        val altitude = moonPosition.altitude
        val distance = moonPosition.distance
        val firstQuarter =
            MoonPhase.compute().phase(MoonPhase.Phase.FIRST_QUARTER).on(nowLong)
                .execute().time
        val lastQuarter =
            MoonPhase.compute().phase(MoonPhase.Phase.LAST_QUARTER).on(nowLong)
                .execute().time
        val illumination = (param.fraction * 100.0).roundToInt()
        var painter: Painter
        //fullMoon.time - nowLong.time < newMoon.time - nowLong.time
        if (fullMoon.time < newMoon.time) {
            val id = GetMoonImage.imageToFull(illumination)
            saveInformationOfTheMoon(illumination, angle.toFloat(), true)
            painter = painterResource(id = id)
            //saveInformationOfTheMoon(illumination, angle.toFloat(), true)
            // full moon incoming
            if (illumination == 0) {
                test = stringResource(R.string.new_moon)
            } else if (illumination in 1..49) {
                test = stringResource(R.string.waxing_crescent)
            } else if (illumination == 50) {
                test = stringResource(R.string.first_quarter)
            } else if (illumination in 51..99) {
                test = stringResource(R.string.waxing_gibbous)
            } else {
                test = stringResource(R.string.full_moon)
            }
        } else {
            val id = GetMoonImage.imageToNew(illumination)
            saveInformationOfTheMoon(illumination, angle.toFloat(), false)
            painter = painterResource(id = id)
            //saveInformationOfTheMoon(illumination, angle.toFloat(), false)
            //saveLocationOnDataStore(id, angle)
            // new moon incoming
            if (illumination == 0) {
                test = stringResource(R.string.new_moon)
            } else if (illumination in 1..49) {
                test = stringResource(R.string.waning_crescent)
            } else if (illumination == 50) {
                test = stringResource(R.string.last_quarter)
            } else if (illumination in 51..98) {
                test = stringResource(R.string.waning_gibbous)
            } else {
                test = stringResource(R.string.full_moon)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MyBlack)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                text = str,
                style = TextStyle(color = Color.White, fontSize = 12.sp)
            )

            val moonTime =
                MoonTimes
                    .compute()
                    .on(calendar.time)
                    .midnight()
                    .at(currentLocation.lat, currentLocation.lng)
                    .execute()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (moonTime.rise != null) {
                    val rise = Calendar.getInstance()
                    rise.time = moonTime.rise!!
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.moonrise),
                            contentDescription = "",
                            modifier = Modifier
                                .size(28.dp),
                            tint = Color.LightGray
                        )
                        val formatterRise = DateFormat.getTimeInstance(DateFormat.SHORT)
                        val time = formatterRise.format(rise.time)
                        Text(
                            text = time,
                            style = TextStyle(color = Color.White, fontSize = 10.sp)
                        )
                    }
                }
                if (moonTime.set != null) {
                    val set = Calendar.getInstance()
                    set.time = moonTime.set!!
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.moonset),
                            contentDescription = "",
                            modifier = Modifier
                                .size(28.dp),
                            tint = Color.LightGray
                        )
                        val formatterSet = DateFormat.getTimeInstance(DateFormat.SHORT)
                        val time = formatterSet.format(set.time)
                        Text(
                            text = time,
                            style = TextStyle(color = Color.White),
                            fontSize = 10.sp
                        )
                    }
                }
            }
            Image(
                painter = painter,
                contentDescription = "",
                //alpha = alpha,
                modifier = Modifier
                    .size(250.dp)
                    //.scale(scale)
                    .rotate(angle.toFloat())
            )
            Text(
                modifier = Modifier.padding(top = 10.dp),
                text = test,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.roboto_medium))
                )
            )
            Text(
                modifier = Modifier.padding(top = 10.dp),
                text = "$illumination%",
                style = TextStyle(color = Color.White, fontSize = 14.sp)
            )
            MoreInformation(azimuth, altitude, distance, painter, angle)
            Column(
                modifier = Modifier
                    //.background(color = Color.Cyan)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider(
                    color = DividerColor, modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(start = 50.dp, end = 50.dp)
                )
                Text(
                    modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                    text = "Upcoming phases",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.roboto_regular))
                    )
                )
                val phases = mutableListOf<PhaseModel>()
                phases.add(PhaseModel(newMoon, getString(R.string.new_moon)))
                phases.add(PhaseModel(firstQuarter, getString(R.string.first_quarter)))
                phases.add(PhaseModel(fullMoon, getString(R.string.full_moon)))
                phases.add(PhaseModel(lastQuarter, getString(R.string.last_quarter)))
                phases.sortBy { it.date }
                val row1 = mutableListOf<PhaseModel>()
                row1.add(phases[0])
                row1.add(phases[1])
                row1.sortBy { it.date }
                val row2 = mutableListOf<PhaseModel>()
                row2.add(phases[2])
                row2.add(phases[3])
                row1.sortBy { it.date }
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    items(phases) {
                        if (it.name == getString(R.string.new_moon)) {
                            val moonPhasePosition =
                                MoonPosition.compute().on(it.date)
                                    .at(currentLocation.lat, currentLocation.lng)
                                    .execute()
                            Phase(
                                id = R.drawable.moon_to_full_0,
                                if (currentLocation.lat < 0) 180F else 0F,
                                it.date,
                                it.name
                            )
                        }
                        if (it.name == getString(R.string.first_quarter)) {
                            val moonPhasePosition =
                                MoonPosition.compute().on(it.date)
                                    .at(currentLocation.lat, currentLocation.lng)
                                    .execute()
                            Phase(
                                id = R.drawable.moon_to_full_50,
                                if (currentLocation.lat < 0) 165F else 0F,
                                it.date,
                                it.name
                            )
                        }
                        if (it.name == getString(R.string.full_moon)) {
                            val moonPhasePosition =
                                MoonPosition.compute().on(it.date)
                                    .at(currentLocation.lat, currentLocation.lng)
                                    .execute()
                            Phase(
                                id = R.drawable.moon_100,
                                if (currentLocation.lat < 0) 180F else 0F,
                                it.date,
                                it.name
                            )
                        }
                        if (it.name == getString(R.string.last_quarter)) {
                            val moonPhasePosition =
                                MoonPosition.compute().on(it.date)
                                    .at(currentLocation.lat, currentLocation.lng)
                                    .execute()
                            Phase(
                                id = R.drawable.moon_to_new_50,
                                if (currentLocation.lat < 0) 185F else 0F,
                                it.date,
                                it.name
                            )
                        }
                    }
                }
            }
        }
    }

    private fun saveLocationOnDataStore(latitude: Double, longitude: Double) {
        val pref = dataStore
        lifecycleScope.launch(Dispatchers.IO) {
            pref.edit {
                it[doublePreferencesKey(getString(R.string.latitude))] = latitude
                it[doublePreferencesKey(getString(R.string.longitude))] = longitude
                //CurrentMoonWidget.updateAll(this@MainActivity)
            }
        }
    }

    private fun saveInformationOfTheMoon(illumination: Int, angle: Float, fullMoonComingSoon: Boolean) {
        val pref = dataStore
        lifecycleScope.launch(Dispatchers.IO) {
            pref.edit {
                it[intPreferencesKey(getString(R.string.moon_illumination))] = illumination
                it[intPreferencesKey(getString(R.string.moon_angle))] = angle.roundToInt()
                it[booleanPreferencesKey(getString(R.string.full_moon_coming_soon))] = fullMoonComingSoon
                CurrentMoonWidget.updateAll(this@MainActivity)
            }
        }
    }

    private fun saveImage(id: Int, degrees: Float) {
        val originalBitmap = BitmapFactory.decodeResource(resources, id)
        val matrix = Matrix().apply {
            postRotate(degrees)
        }
        val bitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
        val encodedImage = Encode.encodeImage(bitmap = bitmap)
        lifecycleScope.launch(Dispatchers.IO) {
            val pref = dataStore
            pref.edit {
                it[stringPreferencesKey("Image")] = encodedImage
                CurrentMoonWidget.updateAll(this@MainActivity)
            }
        }
    }

    private fun imageForWidget(id: Int, degrees: Float) {
        val drawableOriginal = ContextCompat.getDrawable(this@MainActivity, id)
        val originalBitmap = (drawableOriginal as BitmapDrawable).bitmap
        val matrix = Matrix().apply {
            postRotate(degrees)
        }
        val bitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
        try {
            val dir = filesDir
            val file = File(dir, "image_widget")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 0, fos)
            fos.close()
            lifecycleScope.launch { CurrentMoonWidget.updateAll(this@MainActivity) }
        } catch (e: Exception) {
            //e.localizedMessage?.let { Log.d("pepe", it) }
            e.printStackTrace()
        }
    }

    @Composable
    fun MoreInformation(
        azimuth: Double,
        altitude: Double,
        distance: Double,
        painter: Painter,
        angle: Double
    ) {
        Column(
            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Divider(
                color = DividerColor, modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(start = 50.dp, end = 50.dp)
            )
            /*Text(
                modifier = Modifier.padding(5.dp),
                text = "More Information",
                style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold)
            )*/
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 2.dp),
                    text = stringResource(R.string.azimuth),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.roboto_regular))
                    )
                )
                Text(
                    text = "${(azimuth.roundToInt())}°",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp
                    )
                )
            }
            Box(
                modifier = Modifier
                    .rotate(azimuth.toFloat())
                    .size(160.dp)
                    .background(color = MyBlack, shape = CircleShape)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painter = painterResource(id = R.drawable.compass),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxSize()
                        .clip(CircleShape)
                        .align(Alignment.Center)
                        .rotate(360f - azimuth.toFloat())
                )
                Box(
                    modifier = Modifier
                        .background(color = Color.White, shape = CircleShape)
                        .size(14.dp)
                        .align(Alignment.TopCenter)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 2.dp),
                        text = stringResource(R.string.altitude),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.roboto_regular))
                        )
                    )
                    Text(
                        text = "${(altitude.roundToInt())}°",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 12.sp,
                        )
                    )
                }
            }
            Divider(
                color = DividerColor, modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(start = 50.dp, end = 50.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 2.dp),
                        text = stringResource(R.string.distance),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.roboto_regular))
                        )
                    )
                    Text(
                        text = "${(distance.roundToInt())}km",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 12.sp,
                        )
                    )
                }
            }
        }
    }

    @Composable
    private fun Phase(id: Int, angle: Float, date: Date, name: String) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp)
                .background(MyBlack)
                .padding(2.dp)
                .background(color = NextPhases, shape = RoundedCornerShape(8.dp))
                .border(width = 2.dp, color = NextPhases, shape = RoundedCornerShape(8.dp))
                .padding(10.dp)
                .background(NextPhases),
            //.padding(2.dp)
            //.background(MyBlack2)
            // .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val formatterDate = DateFormat.getDateInstance()
            val dateAux = formatterDate.format(date)
            val formatterTime = DateFormat.getTimeInstance(DateFormat.SHORT)
            val time = formatterTime.format(date)
            Text(
                modifier = Modifier.padding(top = 10.dp, bottom = 5.dp, start = 10.dp, end = 10.dp),
                text = name,
                style = TextStyle(color = Color.White, fontSize = 12.sp)
            )
            Image(
                painter = rememberGlidePainter(request = id,
                    requestBuilder = { apply(optionsCache) }),
                contentDescription = "",
                // alpha = 0.75f,
                modifier = Modifier
                    .size(32.dp)
                    .rotate(angle)
                    .padding()
            )
            Text(
                modifier = Modifier.padding(top = 5.dp, start = 10.dp, end = 10.dp),
                text = dateAux,
                style = TextStyle(color = Color.White, fontSize = 12.sp)
            )
            Text(
                modifier = Modifier.padding(top = 2.dp, start = 10.dp, end = 10.dp, bottom = 10.dp),
                text = time,
                style = TextStyle(color = Color.White, fontSize = 12.sp)
            )
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