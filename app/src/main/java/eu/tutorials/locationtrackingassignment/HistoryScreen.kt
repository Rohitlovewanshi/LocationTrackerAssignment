package eu.tutorials.locationtrackingassignment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.LatLng

@Composable
fun HistoryScreen(
    navController: NavHostController,
    navigateToMap: (LatLng,LatLng) -> Unit
) {

    var hItems by remember {
        mutableStateOf(listOf<HistoryItem>())
    }

    hItems = hItems + HistoryItem("30-01-2024 00:00:00", "34.76","98.867", "5645.76","876.878")

    lateinit var historyList: List<HistoryItem>
    historyList = ArrayList<HistoryItem>()

    val dbHandler: DBHandler = DBHandler(LocalContext.current);
    historyList = dbHandler.readHistory()!!

    val scaffoldState = rememberScaffoldState()

    Scaffold(
        topBar = {
            AppBarView(title = "History"){
                navController.navigateUp()
            }
        },
        scaffoldState = scaffoldState
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(it),
            verticalArrangement = Arrangement.Center
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)

            ){
                items(historyList) {
                        item ->
                    HistoryScreenItem(item,navigateToMap)
                    Divider(color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun HistoryScreenItem(item: HistoryItem, navigateToMap: (LatLng, LatLng) -> Unit) {

    Column(
        modifier = Modifier
            .clickable(
                onClick = {
                    val startCoordinates = LatLng(item.startLatitude.toDouble(),item.startLongitude.toDouble())
                    val endCoordinates = LatLng(item.endLatitude.toDouble(),item.endLongitude.toDouble())
                    navigateToMap(startCoordinates,endCoordinates)
                },
            )
            .padding(8.dp)
            .fillMaxWidth()

    ){
        Text(
            modifier = Modifier.padding(bottom = 5.dp),
            text = "Date Time : ${item.dateAndTime}",
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Start Coordinates : ${item.startLatitude}/${item.startLongitude}",
            fontSize = 14.sp
        )
        Text(
            text = "End Coordinates : ${item.endLatitude}/${item.endLongitude}",
            fontSize = 14.sp
        )
    }
}