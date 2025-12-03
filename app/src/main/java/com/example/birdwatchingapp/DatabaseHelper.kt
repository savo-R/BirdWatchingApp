package com.example.birdwatchingapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "birdwatcher.db"
        private const val DATABASE_VERSION = 2

        // TRIPS TABLE
        private const val TABLE_TRIPS = "trips"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TRIP_NAME = "trip_name"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_LOCATION = "location"
        private const val COLUMN_DURATION = "duration"
        private const val COLUMN_DESCRIPTION = "description"

        // BIRD SIGHTINGS TABLE
        private const val TABLE_BIRD_SIGHTINGS = "bird_sightings"
        private const val COLUMN_SIGHTING_ID = "id"
        private const val COLUMN_TRIP_ID = "trip_id"
        private const val COLUMN_SPECIES = "species"
        private const val COLUMN_QUANTITY = "quantity"
        private const val COLUMN_COMMENTS = "comments"
        private const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create trips table
        val createTripsTable = """
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

        // Create bird sightings table
        val createBirdSightingsTable = """
            CREATE TABLE $TABLE_BIRD_SIGHTINGS (
                $COLUMN_SIGHTING_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TRIP_ID INTEGER,
                $COLUMN_SPECIES TEXT,
                $COLUMN_QUANTITY INTEGER,
                $COLUMN_COMMENTS TEXT,
                $COLUMN_TIMESTAMP INTEGER,
                FOREIGN KEY($COLUMN_TRIP_ID) REFERENCES $TABLE_TRIPS($COLUMN_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db?.execSQL(createTripsTable)
        db?.execSQL(createBirdSightingsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Drop tables if they exist
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_BIRD_SIGHTINGS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TRIPS")
        onCreate(db)
    }

    // ==================== TRIPS METHODS ====================

    // Insert trip
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

    // Get all trips
    fun getAllTrips(): List<Trip> {
        val tripList = mutableListOf<Trip>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_TRIPS ORDER BY $COLUMN_ID DESC"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val trip = Trip(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    tripName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIP_NAME)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                    location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                    duration = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                )
                tripList.add(trip)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return tripList
    }

    // Get trip count
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

    // Delete trip by id
    fun deleteTrip(tripId: Int): Int {
        val db = this.writableDatabase
        val result = db.delete(TABLE_TRIPS, "$COLUMN_ID = ?", arrayOf(tripId.toString()))
        db.close()
        return result
    }

    // Update trip
    fun updateTrip(tripId: Int, tripName: String, date: String, time: String, location: String, duration: String, description: String): Int {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COLUMN_TRIP_NAME, tripName)
        values.put(COLUMN_DATE, date)
        values.put(COLUMN_TIME, time)
        values.put(COLUMN_LOCATION, location)
        values.put(COLUMN_DURATION, duration)
        values.put(COLUMN_DESCRIPTION, description)

        val result = db.update(TABLE_TRIPS, values, "$COLUMN_ID = ?", arrayOf(tripId.toString()))
        db.close()
        return result
    }

    // Get single trip by id
    fun getTripById(tripId: Int): Trip? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_TRIPS WHERE $COLUMN_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(tripId.toString()))

        var trip: Trip? = null
        if (cursor.moveToFirst()) {
            trip = Trip(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                tripName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIP_NAME)),
                date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                duration = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
            )
        }

        cursor.close()
        db.close()
        return trip
    }

    // Get total hours from all trips
    fun getHoursCount(): Double {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_DURATION FROM $TABLE_TRIPS"
        val cursor = db.rawQuery(query, null)

        var totalHours = 0.0
        if (cursor.moveToFirst()) {
            do {
                val durationStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DURATION))
                val hours = parseDuration(durationStr)
                totalHours += hours
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return totalHours
    }

    // Helper function to parse duration string
    private fun parseDuration(duration: String): Double {
        return try {
            duration.toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    // ==================== BIRD SIGHTINGS METHODS ====================

    // Insert bird sighting
    fun insertBirdSighting(tripId: Int, species: String, quantity: Int, comments: String, timestamp: Long): Long {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COLUMN_TRIP_ID, tripId)
        values.put(COLUMN_SPECIES, species)
        values.put(COLUMN_QUANTITY, quantity)
        values.put(COLUMN_COMMENTS, comments)
        values.put(COLUMN_TIMESTAMP, timestamp)

        val result = db.insert(TABLE_BIRD_SIGHTINGS, null, values)
        db.close()
        return result
    }

    // Get all bird sightings
    fun getAllBirdSightings(): List<BirdSighting> {
        val sightingList = mutableListOf<BirdSighting>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_BIRD_SIGHTINGS ORDER BY $COLUMN_TIMESTAMP DESC"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val sighting = BirdSighting(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SIGHTING_ID)),
                    tripId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRIP_ID)),
                    species = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SPECIES)),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY)),
                    comments = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENTS)),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                )
                sightingList.add(sighting)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return sightingList
    }

    // Get bird sightings by trip ID
    fun getBirdSightingsByTripId(tripId: Int): List<BirdSighting> {
        val sightingList = mutableListOf<BirdSighting>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_BIRD_SIGHTINGS WHERE $COLUMN_TRIP_ID = ? ORDER BY $COLUMN_TIMESTAMP DESC"
        val cursor = db.rawQuery(query, arrayOf(tripId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val sighting = BirdSighting(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SIGHTING_ID)),
                    tripId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRIP_ID)),
                    species = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SPECIES)),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY)),
                    comments = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENTS)),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                )
                sightingList.add(sighting)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return sightingList
    }

    // Get bird sighting count
    fun getBirdSightingCount(): Int {
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_BIRD_SIGHTINGS"
        val cursor = db.rawQuery(query, null)

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }

        cursor.close()
        db.close()
        return count
    }

    // Get total bird count (sum of all quantities)
    fun getTotalBirdCount(): Int {
        val db = this.readableDatabase
        val query = "SELECT SUM($COLUMN_QUANTITY) FROM $TABLE_BIRD_SIGHTINGS"
        val cursor = db.rawQuery(query, null)

        var totalCount = 0
        if (cursor.moveToFirst()) {
            totalCount = cursor.getInt(0)
        }

        cursor.close()
        db.close()
        return totalCount
    }

    // Delete bird sighting by id
    fun deleteBirdSighting(sightingId: Int): Int {
        val db = this.writableDatabase
        val result = db.delete(TABLE_BIRD_SIGHTINGS, "$COLUMN_SIGHTING_ID = ?", arrayOf(sightingId.toString()))
        db.close()
        return result
    }

    // Delete all bird sightings for a specific trip
    fun deleteBirdSightingsByTripId(tripId: Int): Int {
        val db = this.writableDatabase
        val result = db.delete(TABLE_BIRD_SIGHTINGS, "$COLUMN_TRIP_ID = ?", arrayOf(tripId.toString()))
        db.close()
        return result
    }

    // Update bird sighting
    fun updateBirdSighting(sightingId: Int, species: String, quantity: Int, comments: String): Int {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COLUMN_SPECIES, species)
        values.put(COLUMN_QUANTITY, quantity)
        values.put(COLUMN_COMMENTS, comments)

        val result = db.update(TABLE_BIRD_SIGHTINGS, values, "$COLUMN_SIGHTING_ID = ?", arrayOf(sightingId.toString()))
        db.close()
        return result
    }

    // Get bird sighting by id
    fun getBirdSightingById(sightingId: Int): BirdSighting? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_BIRD_SIGHTINGS WHERE $COLUMN_SIGHTING_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(sightingId.toString()))

        var sighting: BirdSighting? = null
        if (cursor.moveToFirst()) {
            sighting = BirdSighting(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SIGHTING_ID)),
                tripId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRIP_ID)),
                species = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SPECIES)),
                quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY)),
                comments = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENTS)),
                timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
            )
        }

        cursor.close()
        db.close()
        return sighting
    }
}