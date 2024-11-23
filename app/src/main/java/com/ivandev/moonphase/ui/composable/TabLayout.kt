package com.ivandev.moonphase.ui.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.ivandev.moonphase.R

enum class TabPage(val icon: ImageVector) {
    Home(Icons.Default.Home),
    Calendar(Icons.Default.DateRange),
    Location(Icons.Default.LocationOn)
}

@Composable
fun TabLayout(selectedTabIndex: Int, onSelectedTab: (TabPage) -> Unit) {
    /*TabRow(selectedTabIndex = selectedTabIndex) {
        TabPage.entries.toTypedArray().onEachIndexed { index, tabPage ->
            Tab(
                selected = index == selectedTabIndex,
                onClick = { onSelectedTab(tabPage) },
                icon = { Icon(imageVector = tabPage.icon, contentDescription = null)}
            )
        }
        
    }*/

}