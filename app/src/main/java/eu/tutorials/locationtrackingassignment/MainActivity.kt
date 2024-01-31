package eu.tutorials.locationtrackingassignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import eu.tutorials.locationtrackingassignment.ui.theme.LocationTrackingAssignmentTheme

class MainActivity : ComponentActivity() {

    private lateinit var mFusedLocationClient : FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {

            val currentLocation = remember {
                mutableStateOf(LatLng(0.toDouble(), 0.toDouble()))
            }

            val startLocation = remember {
                mutableStateOf(LatLng(0.toDouble(), 0.toDouble()))
            }

            locationCallback = object: LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    for (location in p0.locations){
                        currentLocation.value = LatLng(location.latitude, location.longitude)
                    }
                    if (startLocation.value.latitude==0.0 && startLocation.value.longitude==0.0)
                        startLocation.value = currentLocation.value
                }
            }

            LocationTrackingAssignmentTheme {
                // A surface container using the 'background' color from the theme
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocationTrackingApp(navController, mFusedLocationClient, locationCallback, currentLocation, startLocation, this@MainActivity)
                }
            }
        }
    }
}