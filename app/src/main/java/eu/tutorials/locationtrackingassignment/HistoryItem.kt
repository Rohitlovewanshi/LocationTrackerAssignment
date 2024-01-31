package eu.tutorials.locationtrackingassignment

data class HistoryItem(
    val dateAndTime: String,
    val startLatitude: String,
    val startLongitude: String,
    val endLatitude: String,
    val endLongitude: String,
)
