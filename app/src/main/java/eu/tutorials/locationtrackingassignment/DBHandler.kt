package eu.tutorials.locationtrackingassignment

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHandler(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "LocationTrackerDB"
        private const val DB_VERSION = 1
        private const val TABLE_NAME = "History"
        private const val ID_COL = "id"
        private const val DATE_TIME_COL = "datetime"
        private const val START_LATITUDE_COL = "start_latitude"
        private const val START_LONGITUDE_COL = "start_longitude"
        private const val END_LATITUDE_COL = "end_latitude"
        private const val END_LONGITUDE_COL = "end_longitude"

    }

    override fun onCreate(db: SQLiteDatabase) {

        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DATE_TIME_COL + " TEXT,"
                + START_LATITUDE_COL + " TEXT,"
                + START_LONGITUDE_COL + " TEXT,"
                + END_LATITUDE_COL + " TEXT,"
                + END_LONGITUDE_COL + " TEXT)")

        db.execSQL(query)
    }

    fun addNewHistory(item: HistoryItem) {

        val db = this.writableDatabase

        val values = ContentValues()
        values.put(DATE_TIME_COL, item.dateAndTime)
        values.put(START_LATITUDE_COL, item.startLatitude)
        values.put(START_LONGITUDE_COL, item.startLongitude)
        values.put(END_LATITUDE_COL, item.endLatitude)
        values.put(END_LONGITUDE_COL, item.endLongitude)

        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    fun readHistory(): ArrayList<HistoryItem>? {
        val db = this.readableDatabase

        val cursorCourses: Cursor = db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $ID_COL DESC", null)

        val courseModelArrayList: ArrayList<HistoryItem> = ArrayList()

        if (cursorCourses.moveToFirst()) {
            do {
                courseModelArrayList.add(
                    HistoryItem(
                        cursorCourses.getString(1),
                        cursorCourses.getString(2),
                        cursorCourses.getString(3),
                        cursorCourses.getString(4),
                        cursorCourses.getString(5),
                    )
                )
            } while (cursorCourses.moveToNext())
        }
        cursorCourses.close()
        return courseModelArrayList
    }
}