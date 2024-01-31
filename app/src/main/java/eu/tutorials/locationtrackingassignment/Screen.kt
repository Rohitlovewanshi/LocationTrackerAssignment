package eu.tutorials.locationtrackingassignment

sealed class Screen(val route: String) {
    object HomeScreen : Screen("homescreen")
    object HistoryScreen : Screen("historyscreen")
    object MapScreen : Screen("mapscreen")
}