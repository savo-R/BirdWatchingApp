package com.example.birdwatchingapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var fabAdd: FloatingActionButton
    private lateinit var tvTripsCount: TextView
    private lateinit var tvBirdsCount: TextView
    private lateinit var tvHoursCount: TextView
    private lateinit var recyclerViewTrips: RecyclerView
    private lateinit var emptyState: LinearLayout

    // database helper
    private lateinit var dbHelper: DatabaseHelper

    // trip list
    private var tripList = mutableListOf<Trip>()
    private lateinit var adapter: TripAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initialize database
        dbHelper = DatabaseHelper(this)

        // initialize views
        fabAdd = findViewById(R.id.fabAdd)
        tvTripsCount = findViewById(R.id.tvTripsCount)
        tvBirdsCount = findViewById(R.id.tvBirdsCount)
        tvHoursCount = findViewById(R.id.tvHoursCount)
        recyclerViewTrips = findViewById(R.id.recyclerViewTrips)
        emptyState = findViewById(R.id.emptyState)

        // setup recyclerview
        recyclerViewTrips.layoutManager = LinearLayoutManager(this)

        // fab click
        fabAdd.setOnClickListener {
            val intent = Intent(this, AddTripActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // refresh data when coming back
        loadStats()
        loadTrips()
    }

    private fun loadStats() {
        // get trip count from database
        val tripCount = dbHelper.getTripCount()

        // get bird statistics from storage
        val totalBirds = BirdSightingStorage.getTotalBirdCount()

        // update UI
        tvTripsCount.text = tripCount.toString()
        tvBirdsCount.text = totalBirds.toString()
        tvHoursCount.text = "0"
    }

    private fun loadTrips() {
        // get trips from database
        tripList = dbHelper.getAllTrips().toMutableList()

        if (tripList.isEmpty()) {
            // show empty state
            emptyState.visibility = View.VISIBLE
            recyclerViewTrips.visibility = View.GONE
        } else {
            // show trip list
            emptyState.visibility = View.GONE
            recyclerViewTrips.visibility = View.VISIBLE

            // setup adapter
            adapter = TripAdapter(tripList) { trip ->
                // navigate to trip details
                val intent = Intent(this, TripDetailsActivity::class.java)
                intent.putExtra("TRIP_ID", trip.id)
                startActivity(intent)
            }
            recyclerViewTrips.adapter = adapter
        }
    }
}