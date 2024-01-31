package eu.tutorials.locationtrackingassignment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import android.provider.Settings
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import eu.tutorials.locationtrackingassignment.ui.theme.green
import eu.tutorials.locationtrackingassignment.ui.theme.red
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
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

    val showDialog = remember { mutableStateOf(false) }
    val trackingInterval = remember { mutableStateOf("1000") }

    if (showDialog.value) {
        ShowDialog(context,trackingInterval, showDialog)
    }

    val permission = arrayOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    val storagePermission = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    val launchMultiplePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {
            permissionMaps ->
        val areGranted = permissionMaps.values.reduce {acc, next -> acc && next}
        if (areGranted){
            if (permissionMaps.keys.containsAll(permission.toList())) {
                startLocationUpdates(locationCallback, mFusedLocationClient, trackingInterval)
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                writeTextToFile(context)
            }
        } else if (permissionMaps.keys.containsAll(permission.toList()))  {
            Toast.makeText(context,"Permission Denied. Please enable location permission to use this functionality.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context,"Permission Denied. Please enable storage permission to use this functionality.", Toast.LENGTH_SHORT).show()
            val intent = Intent()
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts(
                "package", context.getPackageName(),
                null
            )
            intent.setData(uri)
            context.startActivity(intent)
        }
    }

    Scaffold(
        topBar = {
            AppBarView(
                title = "LocationTracker",
                onSetTrackIntervalClicked = { showDialog.value = true },
                onExportClicked = {
                    if (storagePermission.all {
                            ContextCompat.checkSelfPermission(context,it) == PackageManager.PERMISSION_GRANTED
                        }) {
                        writeTextToFile(context)
                    } else {
                        launchMultiplePermissions.launch(storagePermission)
                    }
                },
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

                        DBHandler(context).addNewHistory(historyItem)

                        currentLocation.value = LatLng(0.toDouble(), 0.toDouble())
                        startLocation.value = currentLocation.value

                        Toast.makeText(context,"History created.",Toast.LENGTH_LONG).show()
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
fun ShowDialog(context: Context,trackingInterval: MutableState<String>, showDialog: MutableState<Boolean>) {

    var trackingIntervalTemp by remember { mutableStateOf(trackingInterval.value) }

    val pattern = remember { Regex("^\\d+\$") }

    AlertDialog(onDismissRequest = { showDialog.value = false },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(7.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    if (trackingIntervalTemp.isEmpty()){
                        Toast.makeText(context,"Field can not be empty!!",Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    trackingInterval.value = trackingIntervalTemp
                    showDialog.value = false
                    Toast.makeText(context,"Tracking Interval Changed!!",Toast.LENGTH_LONG).show()
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
                    onValueChange = {
                        if (it.isEmpty() || it.matches(pattern)) {
                            trackingIntervalTemp = it
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
            .setMinUpdateIntervalMillis(trackingInterval.value.toLong())
            .setMaxUpdateAgeMillis(100)
            .build()

        mFusedLocationClient.requestLocationUpdates(
            locationRequest,
            it,
            Looper.getMainLooper()
        )
    }
}
private fun convertClassToJson(context: Context): String? {
    val gson: Gson? = GsonBuilder().serializeNulls().setPrettyPrinting().create()
    lateinit var historyList: List<HistoryItem>
    historyList = ArrayList<HistoryItem>()
    val dbHandler: DBHandler = DBHandler(context);
    historyList = dbHandler.readHistory()!!
    return gson?.toJson(historyList)
}
private fun getRandomFileName(): String {
    return Calendar.getInstance().timeInMillis.toString() + ".json"
}
private fun writeTextToFile(context: Context) {

    val jsonResponse = convertClassToJson(context)

    if (jsonResponse != "") {
        val dir = File("//sdcard//Download//")
        val myExternalFile = File(dir, getRandomFileName())
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(myExternalFile)
            fos.write(jsonResponse?.toByteArray())
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Toast.makeText(context, "Information saved to SD card. $myExternalFile", Toast.LENGTH_SHORT).show()
    }
}