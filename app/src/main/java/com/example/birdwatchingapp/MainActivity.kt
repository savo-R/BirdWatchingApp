package com.example.birdwatchingapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var fabAdd: FloatingActionButton
    private lateinit var btnUploadAll: Button
    private lateinit var tvTripsCount: TextView
    private lateinit var tvBirdsCount: TextView
    private lateinit var tvHoursCount: TextView
    private lateinit var recyclerViewTrips: RecyclerView
    private lateinit var emptyState: LinearLayout

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var firebaseHelper: FirebaseHelper

    private var tripList = mutableListOf<Trip>()
    private lateinit var adapter: TripAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        firebaseHelper = FirebaseHelper(dbHelper)

        fabAdd = findViewById(R.id.fabAdd)
        btnUploadAll = findViewById(R.id.btnUploadAll)
        tvTripsCount = findViewById(R.id.tvTripsCount)
        tvBirdsCount = findViewById(R.id.tvBirdsCount)
        tvHoursCount = findViewById(R.id.tvHoursCount)
        recyclerViewTrips = findViewById(R.id.recyclerViewTrips)
        emptyState = findViewById(R.id.emptyState)

        recyclerViewTrips.layoutManager = LinearLayoutManager(this)

        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddTripActivity::class.java))
        }

        btnUploadAll.setOnClickListener {
            uploadAllTrips()
        }
    }

    override fun onResume() {
        super.onResume()
        loadStats()
        loadTrips()
    }

    private fun loadStats() {
        tvTripsCount.text = dbHelper.getTripCount().toString()

        // get total birds from database
        val allBirds = dbHelper.getAllBirdSightings()
        val totalBirds = allBirds.sumOf { it.quantity }
        tvBirdsCount.text = totalBirds.toString()

        tvHoursCount.text = String.format("%.1f", dbHelper.getHoursCount())
    }

    private fun loadTrips() {
        tripList = dbHelper.getAllTrips().toMutableList()

        if (tripList.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerViewTrips.visibility = View.GONE
            btnUploadAll.isEnabled = false
        } else {
            emptyState.visibility = View.GONE
            recyclerViewTrips.visibility = View.VISIBLE
            btnUploadAll.isEnabled = true

            adapter = TripAdapter(tripList) { trip ->
                val intent = Intent(this, TripDetailsActivity::class.java)
                intent.putExtra("TRIP_ID", trip.id)
                startActivity(intent)
            }
            recyclerViewTrips.adapter = adapter
        }
    }

    private fun uploadAllTrips() {
        if (tripList.isEmpty()) {
            Toast.makeText(this, "No trips to upload", Toast.LENGTH_SHORT).show()
            return
        }

        btnUploadAll.isEnabled = false
        Toast.makeText(this, "Uploading trips...", Toast.LENGTH_SHORT).show()

        firebaseHelper.uploadAllTrips(
            trips = tripList,
            onSuccess = { count ->
                btnUploadAll.isEnabled = true
                Toast.makeText(this, "$count trips uploaded successfully", Toast.LENGTH_LONG).show()
            },
            onFailure = { error ->
                btnUploadAll.isEnabled = true
                Toast.makeText(this, "Upload failed: $error", Toast.LENGTH_LONG).show()
            }
        )
    }
}