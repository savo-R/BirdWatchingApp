package com.example.birdwatchingapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "birdwatcher.db"
        private const val DATABASE_VERSION = 1

        // table name
        private const val TABLE_TRIPS = "trips"

        // columns
        private const val COLUMN_ID = "id"
        private const val COLUMN_TRIP_NAME = "trip_name"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_LOCATION = "location"
        private const val COLUMN_DURATION = "duration"
        private const val COLUMN_DESCRIPTION = "description"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // create trips table
        val createTable = """
            CREATE TABLE $TABLE_TRIPS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TRIP_NAME TEXT,
                $COLUMN_DATE TEXT,
                $COLUMN_TIME TEXT,
                $COLUMN_LOCATION TEXT,
                $COLUMN_DURATION TEXT,
                $COLUMN_DESCRIPTION TEXT
            )
        """.trimIndent()

        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // drop table if exists
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TRIPS")
        onCreate(db)
    }

    // insert trip
    fun insertTrip(tripName: String, date: String, time: String, location: String, duration: String, description: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COLUMN_TRIP_NAME, tripName)
        values.put(COLUMN_DATE, date)
        values.put(COLUMN_TIME, time)
        values.put(COLUMN_LOCATION, location)
        values.put(COLUMN_DURATION, duration)
        values.put(COLUMN_DESCRIPTION, description)

        val result = db.insert(TABLE_TRIPS, null, values)
        db.close()
        return result
    }

    // get trip count
    fun getTripCount(): Int {
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_TRIPS"
        val cursor = db.rawQuery(query, null)

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }

        cursor.close()
        db.close()
        return count
    }
}