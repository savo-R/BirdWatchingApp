package com.example.birdwatchingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class TripDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get trip id from intent
        val tripId = intent.getIntExtra("TRIP_ID", -1)

        // not implemented yet
    }
}