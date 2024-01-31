package eu.tutorials.locationtrackingassignment

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import eu.tutorials.locationtrackingassignment.ui.theme.green
import eu.tutorials.locationtrackingassignment.ui.theme.red
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

@Composable
fun HomeScreen(
    mFusedLocationClient: FusedLocationProviderClient,
    locationCallback: LocationCallback,
    currentLocation: MutableState<LatLng>,
    startLocation: MutableState<LatLng>,
    context: Context,
    navigateToHistory: () -> Unit
) {

    var showDialog = remember { mutableStateOf(false) }
    var trackingInterval = remember { mutableStateOf("1000") }

    if (showDialog.value) {
        ShowDialog(trackingInterval, showDialog)
    }

    val permission = arrayOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    val launchMultiplePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {
            permissionMaps ->
        val areGranted = permissionMaps.values.reduce {acc, next -> acc && next}
        if (areGranted){
            startLocationUpdates(locationCallback, mFusedLocationClient, trackingInterval)
            Toast.makeText(context,"Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context,"Permission Denied. Please enable location to start tracking.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            AppBarView(
                title = "LocationTracker",
                onSetTrackIntervalClicked = { showDialog.value = true },
                onExitClicked = {
                    MainActivity().finish()
                    exitProcess(0)
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
        ) {
            Column(
                modifier = Modifier
                    .weight(2.0f, true)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (currentLocation.value.latitude!=0.0 || currentLocation.value.longitude!=0.0)
                    MapView(currentLocation = currentLocation)
                else {
                    Text(
                        text = "Tracking Offline",
                        color = Color.Black,
                        modifier = Modifier
                            .padding(bottom = 50.dp),
                        fontSize = 25.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1.0f, true)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(green),
                    onClick = {

                        if (currentLocation.value.latitude!=0.0 || currentLocation.value.longitude!=0.0) {
                            Toast.makeText(context,"Tracking already started!!",Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        if (permission.all {
                                ContextCompat.checkSelfPermission(context,it) == PackageManager.PERMISSION_GRANTED
                            }) {
                            startLocationUpdates(locationCallback,mFusedLocationClient,trackingInterval)
                            Toast.makeText(context,"Tracking Started.",Toast.LENGTH_LONG).show()
                        } else {
                            launchMultiplePermissions.launch(permission)
                        }
                    },

                    ) {
                    Text(text = "Start Tracking")
                }

                Button(
                    modifier = Modifier.padding(top = 5.dp),
                    colors = ButtonDefaults.buttonColors(red),
                    onClick = {

                        if (currentLocation.value.latitude==0.0 && currentLocation.value.longitude==0.0) {
                            Toast.makeText(context,"Tracking already stopped!!",Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        locationCallback.let {
                            mFusedLocationClient.removeLocationUpdates(it)
                        }

                        Toast.makeText(context,"Tracking Stopped.",Toast.LENGTH_LONG).show()

                        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss",
                            Locale.getDefault())
                        val currentDateAndTime = sdf.format(Date()).toString()

                        val historyItem  = HistoryItem(
                            dateAndTime = currentDateAndTime,
                            startLatitude = startLocation.value.latitude.toString(),
                            startLongitude = startLocation.value.longitude.toString(),
                            endLatitude = currentLocation.value.latitude.toString(),
                            endLongitude = currentLocation.value.longitude.toString()
                        )

                        insertHistoryToDatabase(context,historyItem)

                        currentLocation.value = LatLng(0.toDouble(), 0.toDouble())
                        startLocation.value = currentLocation.value
                    },
                ) {
                    Text(text = "Stop Tracking")
                }
            }

            Column(
                modifier = Modifier
                    .weight(1.0f, true)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp),
                    onClick = {
                        navigateToHistory()
                    }) {
                    Text(text = "History")
                }
            }
        }
    }
}

@Composable
fun ShowDialog(trackingInterval: MutableState<String>, showDialog: MutableState<Boolean>) {

    var trackingIntervalTemp by remember { mutableStateOf(trackingInterval.value) }

    AlertDialog(onDismissRequest = { showDialog.value = false },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(7.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    trackingInterval.value = trackingIntervalTemp
                    showDialog.value = false
                }) {
                    Text(text = "Set")
                }
                Button(onClick = { showDialog.value = false }) {
                    Text(text = "Cancel")
                }
            }
        },
        title = { Text(text = "Set Tracking Interval")},
        text = {
            Column {
                OutlinedTextField(
                    value = trackingIntervalTemp,
                    onValueChange = { trackingIntervalTemp = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }
    )
}

@Composable
fun MapView(currentLocation: MutableState<LatLng>){

    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation.value, 16f)
    }

    Box(Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true
            )
        )
    }
}

@SuppressLint("MissingPermission")
private fun startLocationUpdates(
    locationCallback: LocationCallback,
    mFusedLocationClient: FusedLocationProviderClient,
    trackingInterval: MutableState<String>
) {

    locationCallback.let {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 100
        )
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(trackingInterval.value.toString().toLong())
            .setMaxUpdateAgeMillis(100)
            .build()

        mFusedLocationClient.requestLocationUpdates(
            locationRequest,
            it,
            Looper.getMainLooper()
        )
    }
}

fun insertHistoryToDatabase(context: Context, item: HistoryItem) {

    val dbHandler = DBHandler(context)

    dbHandler.addNewHistory(
        item.dateAndTime,
        item.startLatitude,
        item.startLongitude,
        item.endLatitude,
        item.endLongitude
    )

    Toast.makeText(context,"History created.",Toast.LENGTH_LONG).show()
}
