package com.example.birdwatchingapp

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference

class FirebaseHelper(private val dbHelper: DatabaseHelper) {

    private val database: DatabaseReference

    init {
        val databaseUrl = "https://birdwatchingapp-3b090-default-rtdb.firebaseio.com/"

        database = try {
            Log.d("FirebaseHelper", "Connecting to Firebase: $databaseUrl")
            FirebaseDatabase.getInstance(databaseUrl).reference
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error initializing Firebase: ${e.message}")
            throw e
        }


        try {
            FirebaseDatabase.getInstance(databaseUrl).setPersistenceEnabled(true)
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Persistence already enabled")
        }
    }

    // upload single trip with its bird sightings
    fun uploadTrip(trip: Trip, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        Log.d("FirebaseHelper", "Starting upload for trip: ${trip.tripName}")

        val tripId = trip.id.toString()

        // get bird sightings for this trip
        val birdSightings = dbHelper.getBirdSightingsByTripId(trip.id)
        Log.d("FirebaseHelper", "Found ${birdSightings.size} bird sightings for trip $tripId")

        val tripData = hashMapOf<String, Any>(
            "id" to trip.id,
            "tripName" to trip.tripName,
            "date" to trip.date,
            "time" to trip.time,
            "location" to trip.location,
            "duration" to trip.duration,
            "description" to trip.description
        )

        // add bird sightings if any
        if (birdSightings.isNotEmpty()) {
            val birdsMap = hashMapOf<String, Any>()
            birdSightings.forEach { bird ->
                birdsMap[bird.id.toString()] = hashMapOf(
                    "id" to bird.id,
                    "species" to bird.species,
                    "quantity" to bird.quantity,
                    "comments" to bird.comments,
                    "timestamp" to bird.timestamp
                )
            }
            tripData["birdSightings"] = birdsMap
        }

        Log.d("FirebaseHelper", "Uploading to path: trips/$tripId")

        database.child("trips").child(tripId).setValue(tripData)
            .addOnSuccessListener {
                Log.d("FirebaseHelper", "Upload successful for trip $tripId")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseHelper", "Upload failed: ${e.message}", e)
                onFailure(e.message ?: "Upload failed")
            }
    }

    // upload all trips with their bird sightings
    fun uploadAllTrips(trips: List<Trip>, onSuccess: (Int) -> Unit, onFailure: (String) -> Unit) {
        if (trips.isEmpty()) {
            Log.w("FirebaseHelper", "No trips to upload")
            onFailure("No trips to upload")
            return
        }

        Log.d("FirebaseHelper", "Starting upload for ${trips.size} trips")

        val updates = hashMapOf<String, Any>()

        trips.forEach { trip ->
            val tripId = trip.id.toString()

            // get bird sightings for this trip
            val birdSightings = dbHelper.getBirdSightingsByTripId(trip.id)
            Log.d("FirebaseHelper", "Trip $tripId has ${birdSightings.size} bird sightings")

            val tripData = hashMapOf<String, Any>(
                "id" to trip.id,
                "tripName" to trip.tripName,
                "date" to trip.date,
                "time" to trip.time,
                "location" to trip.location,
                "duration" to trip.duration,
                "description" to trip.description
            )

            // add bird sightings if any
            if (birdSightings.isNotEmpty()) {
                val birdsMap = hashMapOf<String, Any>()
                birdSightings.forEach { bird ->
                    birdsMap[bird.id.toString()] = hashMapOf(
                        "id" to bird.id,
                        "species" to bird.species,
                        "quantity" to bird.quantity,
                        "comments" to bird.comments,
                        "timestamp" to bird.timestamp
                    )
                }
                tripData["birdSightings"] = birdsMap
            }

            updates["trips/$tripId"] = tripData
        }

        Log.d("FirebaseHelper", "Uploading ${updates.size} trips to Firebase")

        database.updateChildren(updates)
            .addOnSuccessListener {
                Log.d("FirebaseHelper", "All trips uploaded successfully")
                onSuccess(trips.size)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseHelper", "Upload failed: ${e.message}", e)
                onFailure(e.message ?: "Upload failed")
            }
    }
}