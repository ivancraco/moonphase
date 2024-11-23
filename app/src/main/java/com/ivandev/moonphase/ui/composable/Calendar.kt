package com.ivandev.moonphase.ui.composable

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.accompanist.glide.rememberGlidePainter
import com.ivandev.moonphase.R
import com.ivandev.moonphase.model.GetDayOfMonth
import com.ivandev.moonphase.model.GetMoonImage
import com.ivandev.moonphase.model.LatLng
import com.ivandev.moonphase.ui.theme.DividerColor
import com.ivandev.moonphase.ui.theme.MyBlack3
import com.ivandev.moonphase.ui.theme.MyWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.shredzone.commons.suncalc.MoonIllumination
import org.shredzone.commons.suncalc.MoonPhase
import org.shredzone.commons.suncalc.MoonPosition
import org.shredzone.commons.suncalc.MoonTimes
import java.text.DateFormat
import java.util.Calendar
import kotlin.math.roundToInt

val optionsCache = RequestOptions().diskCacheStrategy(
    DiskCacheStrategy.ALL
)

val auxCalendar = Calendar.getInstance()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calendar(currentLocation: LatLng) {
    if (currentLocation.lat == 0.0 || currentLocation.lng == 0.0) return
    var number by remember {
        mutableIntStateOf(0)
    }
    val mapOfPhases = mutableListOf<Int>()
    val mapRowOptions = mutableListOf<String>()
    var mapOfPhases2 by remember {
        mutableStateOf(listOf<Int>())
    }
    var mapRowOptions2 by remember {
        mutableStateOf(listOf<String>())
    }
    var isValueReady by remember { mutableStateOf(false) }
    var isMoonOptionsReady by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    calendar.set(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + number,
        1,
        0,
        0,
        0
    )

    val riseMoon = stringResource(R.string.rise_moon)
    val setMoon = stringResource(R.string.set_moon)
    val nMoon = stringResource(R.string.newM)
    val fMoon = stringResource(R.string.fullM)
    val iMoon = stringResource(id = R.string.moon_illumination)
    var rowOptions by remember { mutableStateOf<String>(iMoon) }

    val dayOne = calendar.get(Calendar.DAY_OF_WEEK)
    val listOfDays = mutableListOf<Int>()
    for (num in 1..GetDayOfMonth.daysOfMonth(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH)
    )) {
        listOfDays.add(num)
    }

    val moonTimes =
        MoonTimes
            .compute()
            .on(calendar.time)
            .at(currentLocation.lat, currentLocation.lng)

    val moonPosition =
        MoonPosition.compute().at(currentLocation.lat, currentLocation.lng)

    LaunchedEffect(key1 = isValueReady, block = {
        if (isValueReady) return@LaunchedEffect
        val calendar2 = Calendar.getInstance()
        calendar2.timeInMillis = calendar.timeInMillis
        calendar2.set(Calendar.HOUR_OF_DAY, 12)
        calendar2.set(Calendar.MINUTE, 0)
        calendar2.set(Calendar.SECOND, 0)
        val parameters = MoonIllumination.compute().on(calendar2.time)
        withContext(Dispatchers.Default) {
            mapOfPhases.clear()
            for (num in 1..listOfDays.size) {
                val percent = (parameters.execute().fraction * 100.0).roundToInt()
                mapOfPhases.add(percent)
                parameters.plusDays(1)
            }
        }
        mapOfPhases2 = mapOfPhases
        isValueReady = true
    })

    val formatter = DateFormat.getTimeInstance(DateFormat.SHORT)
    val rise: Calendar = Calendar.getInstance()

    LaunchedEffect(key1 = rowOptions, isValueReady, block = {
        if (rowOptions == iMoon) {
            if (isMoonOptionsReady) return@LaunchedEffect
            val calendar2 = Calendar.getInstance()
            calendar2.timeInMillis = calendar.timeInMillis
            calendar2.set(Calendar.HOUR_OF_DAY, 12)
            calendar2.set(Calendar.MINUTE, 0)
            calendar2.set(Calendar.SECOND, 0)
            val newMoon =
                MoonPhase.compute().phase(MoonPhase.Phase.NEW_MOON)
            val fullMoon =
                MoonPhase.compute().phase(MoonPhase.Phase.FULL_MOON)
            val parameters = MoonIllumination.compute().on(calendar2.time)
            withContext(Dispatchers.Default) {
                mapRowOptions.clear()
                val ca = Calendar.getInstance()
                ca.timeInMillis = calendar.timeInMillis
                val ca2 = Calendar.getInstance()
                ca2.timeInMillis = calendar.timeInMillis
                for (num in 1..listOfDays.size) {
                    ca.set(Calendar.DATE, num)
                    val nAux = newMoon.on(ca.time).execute().time.time
                    val fAux = fullMoon.on(ca.time).execute().time.time
                    ca2.set(Calendar.DATE, num)
                    ca2.set(Calendar.HOUR_OF_DAY, 23)
                    ca2.set(Calendar.MINUTE, 59)
                    ca2.set(Calendar.SECOND, 59)
                    if (nAux >= ca.time.time && nAux <= ca2.time.time) {
                        mapRowOptions.add(nMoon)
                    } else if (fAux >= ca.time.time && fAux <= ca2.time.time) {
                        mapRowOptions.add(fMoon)
                    } else {
                        val percent = (parameters.execute().fraction * 100.0).roundToInt()
                        mapRowOptions.add("${percent}%")
                    }
                    parameters.plusDays(1)
                }
            }
            mapRowOptions2 = mapRowOptions
            isMoonOptionsReady = true
        }
        if (rowOptions == riseMoon) {
            if (isMoonOptionsReady) return@LaunchedEffect
            withContext(Dispatchers.Default) {
                mapRowOptions.clear()
                for (num in 1..listOfDays.size) {
                    if (moonTimes.execute().rise != null) {
                        rise.time = moonTimes.execute().rise!!
                        val time = formatter.format(rise.time)
                        mapRowOptions.add(time)
                    } else mapRowOptions.add("")
                    moonTimes.plusDays(1)
                }
            }
            mapRowOptions2 = mapRowOptions
            isMoonOptionsReady = true
        }
        if (rowOptions == setMoon) {
            if (isMoonOptionsReady) return@LaunchedEffect
            withContext(Dispatchers.Default) {
                mapRowOptions.clear()
                for (num in 1..listOfDays.size) {
                    if (moonTimes.execute().set != null) {
                        rise.time = moonTimes.execute().set!!
                        val time = formatter.format(rise.time)
                        mapRowOptions.add(time)
                    } else mapRowOptions.add("")
                    moonTimes.plusDays(1)
                }
            }
            mapRowOptions2 = mapRowOptions
            isMoonOptionsReady = true
        }
    })

    Column(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        /*var showDatePicker by remember { mutableStateOf(false) }
        val state = rememberDatePickerState()
        if (showDatePicker) {
            DatePickerDialog(onDismissRequest = { Log.d("pepe", "1") }, confirmButton = { Log.d("pepe", "2") }) {
                DatePicker(state = state)
            }
        }*/
        GetDate(year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH))
        var isClickableLeft by remember { mutableStateOf(true) }
        var isClickableRight by remember { mutableStateOf(true) }
        val coroutine = rememberCoroutineScope()
        RowOptions(
            changeMonthLeft = {
                if (isClickableLeft) {
                    number -= 1
                    isValueReady = false
                    isMoonOptionsReady = false
                    isClickableLeft = false
                    coroutine.launch {
                        delay(600)
                        isClickableLeft = true
                    }
                }
            },
            moonIllumination = {
                if (rowOptions != iMoon) {
                    rowOptions = iMoon
                    isMoonOptionsReady = false
                }
            },
            moonRise = {
                if (rowOptions != riseMoon) {
                    rowOptions = riseMoon
                    isMoonOptionsReady = false
                }
            },
            moonSet = {
                if (rowOptions != setMoon) {
                    rowOptions = setMoon
                    isMoonOptionsReady = false
                }
            },
            changeMonthRight = {
                if (isClickableRight) {
                    number += 1
                    isValueReady = false
                    isMoonOptionsReady = false
                    isClickableRight = false
                    coroutine.launch {
                        delay(600)
                        isClickableRight = true
                    }
                }
            })
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
        ) {
            item {
                BoxDayOfWeek(day = stringResource(R.string.sunday))
            }
            item {
                BoxDayOfWeek(day = stringResource(R.string.monday))
            }
            item {
                BoxDayOfWeek(day = stringResource(R.string.tuesday))
            }
            item {
                BoxDayOfWeek(day = stringResource(R.string.wednesday))
            }
            item {
                BoxDayOfWeek(day = stringResource(R.string.thursday))
            }
            item {
                BoxDayOfWeek(day = stringResource(R.string.friday))
            }
            item {
                BoxDayOfWeek(day = stringResource(R.string.saturday))
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .background(MyBlack3)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isValueReady) {
                LazyVerticalGrid(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    columns = GridCells.Fixed(7), content = {
                        val first = if (dayOne == 1) 0 else dayOne - 1
                        for (num in 1..first) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .background(MyBlack3)
                                        .size(70.dp)
                                )
                            }
                        }
                        val calendarAngle = Calendar.getInstance()
                        calendarAngle.timeInMillis = calendar.timeInMillis
                        calendarAngle.set(Calendar.HOUR_OF_DAY, 21)
                        calendarAngle.set(Calendar.MINUTE, 0)
                        calendarAngle.set(Calendar.SECOND, 0)
                        items(listOfDays) {
                            BoxDay(
                                rowOptions = if (isMoonOptionsReady) mapRowOptions2[it - 1] else "",
                                day = it,
                                percent = mapOfPhases2[it - 1],
                                isNewNext = isNewNext(it, calendar),
                                angle = if (currentLocation.lat < 0) 180F else 0F,
                                currMonth = calendar.get(Calendar.MONTH)
                            )
                        }
                    }, horizontalArrangement = Arrangement.Center
                )
            } else {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.loading_message),
                    style = TextStyle(
                        color = MyWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

fun isNewNext(num: Int, calendar: Calendar): Boolean {
    val ca = Calendar.getInstance()
    ca.timeInMillis = calendar.timeInMillis
    val newMoon =
        MoonPhase.compute().phase(MoonPhase.Phase.NEW_MOON)
    val fullMoon =
        MoonPhase.compute().phase(MoonPhase.Phase.FULL_MOON)
    ca.set(Calendar.DATE, num)
    val nAux = newMoon.on(ca.time).execute().time.time
    val fAux = fullMoon.on(ca.time).execute().time.time
    return nAux < fAux
}

fun getMoonAngle(date: Int, calendar: Calendar, moonPosition: MoonPosition.Parameters): Double {
    calendar.set(Calendar.DATE, date)
    return moonPosition.on(calendar.time).execute().parallacticAngle
}


@Composable
fun RowOptions(
    changeMonthLeft: () -> Unit,
    moonIllumination: () -> Unit,
    moonRise: () -> Unit,
    moonSet: () -> Unit,
    changeMonthRight: () -> Unit
) {
    var backgroundColor by remember { mutableIntStateOf(0) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .clickable {
                    changeMonthLeft()
                }
            //.background(MyBlack3)
            ,
            imageVector = Icons.Default.ArrowBack,
            contentDescription = null,
            tint = Color.White
        )
        Icon(
            modifier = Modifier
                .clickable {
                    backgroundColor = 0
                    moonIllumination()
                }
                //.background(if (backgroundColor == 0) MyBlack2 else Color.Transparent)
                .padding(5.dp)
                .size(24.dp),
            painter = painterResource(id = R.drawable.brightness),
            contentDescription = null,
            tint = if (backgroundColor == 0) Color.White else Color.Gray
        )
        Icon(
            modifier = Modifier
                .clickable {
                    backgroundColor = 1
                    moonRise()
                }
                //.background(if (backgroundColor == 1) MyBlack2 else Color.Transparent)
                .padding(5.dp)
                .size(24.dp),
            painter = painterResource(id = R.drawable.moonrise),
            contentDescription = null,
            tint = if (backgroundColor == 1) Color.White else Color.Gray
        )
        Icon(
            modifier = Modifier
                .clickable {
                    backgroundColor = 2
                    moonSet()
                }
                //.background(if (backgroundColor == 2) MyBlack2 else Color.Transparent)
                .padding(5.dp)
                .size(24.dp),
            painter = painterResource(id = R.drawable.moonset),
            contentDescription = null,
            tint = if (backgroundColor == 2) Color.White else Color.Gray
        )
        Icon(
            modifier = Modifier
                .clickable {
                    changeMonthRight()
                }
            //.background(MyBlack3)
            ,
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = Color.White
        )
    }
}

@Composable
fun BoxDayOfWeek(day: String) {
    Box(
        modifier = Modifier
            .width(60.dp)
            .height(40.dp)
            .background(MyBlack3),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            style = TextStyle(color = MyWhite),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun BoxDay(
    rowOptions: String,
    day: Int,
    percent: Int,
    isNewNext: Boolean,
    angle: Float,
    currMonth: Int
) {
    Box(
        modifier = Modifier
            .padding(top = 5.dp, start = 1.dp, end = 1.dp, bottom = 5.dp)
            .background(MyBlack3)
            .padding(top = 5.dp, start = 1.dp, end = 1.dp, bottom = 5.dp)
            .padding(bottom = 1.dp)
            .background(DividerColor)
            .padding(bottom = 1.dp)
            .size(70.dp)
            .background(MyBlack3),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$day",
            style = TextStyle(color = MyWhite, fontSize = 14.sp),
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Image(
            painter = if (isNewNext) {
                rememberGlidePainter(
                    request = GetMoonImage.imageToNew(percent),
                    requestBuilder = { apply(optionsCache) })
            } else rememberGlidePainter(
                request = GetMoonImage.imageToFull(percent),
                requestBuilder = { apply(optionsCache) }),
            contentDescription = "",
            modifier = Modifier
                .width(28.dp)
                .height(28.dp)
                .align(Alignment.Center)
                .rotate(angle)
        )
        Text(
            text = rowOptions,
            maxLines = 1,
            style = TextStyle(color = MyWhite, fontSize = 10.sp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
@Preview(widthDp = 350, heightDp = 450)
fun Preview() {
    Calendar(LatLng(61.38861281532911, 108.01361533580854))
}

@Composable
fun GetDate(year: Int, month: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.padding(top = 10.dp, bottom = 5.dp),
            text = GetDayOfMonth.getNameOfMonth(month),
            style = TextStyle(
                color = MyWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        )
        Text(
            text = "$year",
            style = TextStyle(
                color = Color.Gray,
                fontSize = 12.sp
            )
        )
    }
}