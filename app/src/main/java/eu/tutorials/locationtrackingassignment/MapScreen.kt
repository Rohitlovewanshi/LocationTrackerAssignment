package eu.tutorials.locationtrackingassignment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(
    startCoordinates : LatLng,
    endCoordinates : LatLng,
    navController: NavHostController
){

    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(endCoordinates, 16f)
    }

    val scaffoldState = rememberScaffoldState()

    Scaffold(
        topBar = {
            AppBarView(title = "Map"){
                navController.navigateUp()
            }
        },
        scaffoldState = scaffoldState
    ) {
        Box(Modifier.fillMaxSize().padding(it)) {

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ){

                Marker(
                    state = MarkerState(
                        position = startCoordinates
                    ),
                    title = "Start Coordinates"
                )

                Marker(
                    state = MarkerState(
                        position = endCoordinates
                    ),
                    title = "End Coordinates"
                )

                Polyline(
                    points = listOf(
                        startCoordinates,
                        endCoordinates
                    )
                    ,color = Color.Magenta
                )
            }
        }
    }
}