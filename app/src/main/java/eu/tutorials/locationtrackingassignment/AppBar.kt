package eu.tutorials.locationtrackingassignment

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp

@Composable
fun AppBarView(
    title: String,
    onSetTrackIntervalClicked: () -> Unit = {},
    onExitClicked: () -> Unit = {},
    onBackNavClicked: () -> Unit = {}
){
    val navigationIcon : (@Composable () -> Unit) ?= {
        if (!title.contains("LocationTracker")) {
            IconButton(onClick = { onBackNavClicked() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = Color.White,
                    contentDescription = null
                )
            }
        } else {
            IconButton(onClick = { onBackNavClicked() }) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    tint = Color.White,
                    contentDescription = null
                )
            }
        }

    }

    var mDisplayMenu by remember { mutableStateOf(false) }

    val actions : (@Composable RowScope.() -> Unit) = {
        if (title.contains("LocationTracker")) {
            IconButton(onClick = { mDisplayMenu = !mDisplayMenu }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "",
                    tint = Color.White
                )
            }
            DropdownMenu(
                expanded = mDisplayMenu,
                onDismissRequest = { mDisplayMenu = false }
            ) {

                DropdownMenuItem(onClick = {
                    mDisplayMenu = false
                    onSetTrackIntervalClicked()
                }) {
                    Text(text = "Set Tracking Interval")
                }

                DropdownMenuItem(onClick = {
                    mDisplayMenu = false
                    onExitClicked()
                }) {
                    Text(text = "Exit")
                }
            }
        } else {
            null
        }
    }

    TopAppBar(title = {
        Text(
            text = title,
            color = colorResource(id = R.color.white),
            modifier = Modifier
                .heightIn(max = 24.dp)
        )
    },
        elevation = 3.dp,
        backgroundColor = colorResource(id = R.color.app_bar_color),
        navigationIcon = navigationIcon,
        actions = actions
    )
}