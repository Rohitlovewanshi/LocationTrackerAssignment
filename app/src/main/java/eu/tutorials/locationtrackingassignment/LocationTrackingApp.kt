package eu.tutorials.locationtrackingassignment

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.maps.model.LatLng

@Composable
fun LocationTrackingApp(
    navController: NavHostController,
    mFusedLocationClient: FusedLocationProviderClient,
    locationCallback: LocationCallback,
    currentLocation: MutableState<LatLng>,
    startLocation: MutableState<LatLng>,
    context: Context
) {

    NavHost(navController = navController, startDestination = Screen.HomeScreen.route){
        composable(route = Screen.HomeScreen.route){
            HomeScreen(mFusedLocationClient,locationCallback,currentLocation,startLocation,context) {
                navController.navigate(Screen.HistoryScreen.route)
            }
        }

        composable(route = Screen.HistoryScreen.route){
            HistoryScreen(navController,navigateToMap = {startCoordinates,endCoordinates ->
                navController.currentBackStackEntry?.savedStateHandle?.set("startCoordinates",startCoordinates)
                navController.currentBackStackEntry?.savedStateHandle?.set("endCoordinates",endCoordinates)
                navController.navigate(Screen.MapScreen.route)
            })
        }

        composable(route = Screen.MapScreen.route){
            val startCoordinates = navController.previousBackStackEntry?.savedStateHandle?.get<LatLng>("startCoordinates") ?: LatLng(0.0,0.0)
            val endCoordinates = navController.previousBackStackEntry?.savedStateHandle?.get<LatLng>("endCoordinates") ?: LatLng(0.0,0.0)
            MapScreen(startCoordinates,endCoordinates,navController)
        }
    }
}