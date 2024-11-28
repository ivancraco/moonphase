package com.ivandev.moonphase.ui.composable

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.glide.rememberGlidePainter
import com.ivandev.moonphase.R
import com.ivandev.moonphase.model.GetMoonImage
import com.ivandev.moonphase.model.LatLng
import com.ivandev.moonphase.model.PhaseModel
import com.ivandev.moonphase.ui.theme.DividerColor
import com.ivandev.moonphase.ui.theme.MyBlack
import com.ivandev.moonphase.ui.theme.NextPhases
import org.shredzone.commons.suncalc.MoonIllumination
import org.shredzone.commons.suncalc.MoonPhase
import org.shredzone.commons.suncalc.MoonPosition
import org.shredzone.commons.suncalc.MoonTimes
import java.text.DateFormat
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun CurrentMoon(currentLocation: LatLng) {
    val calendar = java.util.Calendar.getInstance()
    val nowLong = calendar.time
    val newMoon =
        MoonPhase.compute().phase(MoonPhase.Phase.NEW_MOON).on(nowLong)
            .execute().time
    val fullMoon =
        MoonPhase.compute().phase(MoonPhase.Phase.FULL_MOON).on(nowLong)
            .execute().time
    val formatter = DateFormat.getDateInstance()
    val dateFormat = formatter.format(calendar.time)
    val moonPosition =
        MoonPosition.compute().on(nowLong).at(currentLocation.lat, currentLocation.lng)
            .execute()
    val angle =
        moonPosition.parallacticAngle
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
    val painter: Painter
    var moonPhaseName = ""
    if (fullMoon.time < newMoon.time) {
        val id = GetMoonImage.imageToFull(illumination)
        //saveInformationOfTheMoon(illumination, angle.toFloat(), true)
        painter = painterResource(id = id)
        //saveInformationOfTheMoon(illumination, angle.toFloat(), true)
        // full moon incoming
        moonPhaseName = when (illumination) {
            0 -> {
                stringResource(R.string.new_moon)
            }
            in 1..49 -> {
                stringResource(R.string.waxing_crescent)
            }
            50 -> {
                stringResource(R.string.first_quarter)
            }
            in 51..99 -> {
                stringResource(R.string.waxing_gibbous)
            }
            else -> {
                stringResource(R.string.full_moon)
            }
        }
    } else {
        val id = GetMoonImage.imageToNew(illumination)
        //saveInformationOfTheMoon(illumination, angle.toFloat(), false)
        painter = painterResource(id = id)
        //saveInformationOfTheMoon(illumination, angle.toFloat(), false)
        //saveLocationOnDataStore(id, angle)
        // new moon incoming
        moonPhaseName = when (illumination) {
            0 -> {
                stringResource(R.string.new_moon)
            }
            in 1..49 -> {
                stringResource(R.string.waning_crescent)
            }
            50 -> {
                stringResource(R.string.last_quarter)
            }
            in 51..98 -> {
                stringResource(R.string.waning_gibbous)
            }
            else -> {
                stringResource(R.string.full_moon)
            }
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
            text = dateFormat,
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
                val rise = java.util.Calendar.getInstance()
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
                val set = java.util.Calendar.getInstance()
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
            text = moonPhaseName,
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
        MoreInformation(azimuth, altitude, distance)
        Column(
            modifier = Modifier
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
            phases.add(PhaseModel(newMoon, stringResource(R.string.new_moon)))
            phases.add(PhaseModel(firstQuarter, stringResource(R.string.first_quarter)))
            phases.add(PhaseModel(fullMoon, stringResource(R.string.full_moon)))
            phases.add(PhaseModel(lastQuarter, stringResource(R.string.last_quarter)))
            phases.sortBy { it.date }
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items(phases) {
                    if (it.name == stringResource(R.string.new_moon)) {
                        Phase(
                            id = R.drawable.moon_to_full_0,
                            if (currentLocation.lat < 0) 180F else 0F,
                            it.date,
                            it.name
                        )
                    }
                    if (it.name == stringResource(R.string.first_quarter)) {
                        Phase(
                            id = R.drawable.moon_to_full_50,
                            if (currentLocation.lat < 0) 165F else 0F,
                            it.date,
                            it.name
                        )
                    }
                    if (it.name == stringResource(R.string.full_moon)) {
                        Phase(
                            id = R.drawable.moon_100,
                            if (currentLocation.lat < 0) 180F else 0F,
                            it.date,
                            it.name
                        )
                    }
                    if (it.name == stringResource(id = R.string.last_quarter)) {
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

@Composable
fun MoreInformation(
    azimuth: Double,
    altitude: Double,
    distance: Double
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