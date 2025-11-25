package com.example.birdwatchingapp

import android.app.AlertDialog
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
import com.google.android.material.appbar.MaterialToolbar

class TripDetailsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvTripName: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvDescription: TextView
    private lateinit var btnEditTrip: Button
    private lateinit var btnDeleteTrip: Button
    private lateinit var recyclerViewBirds: RecyclerView
    private lateinit var emptyStateBirds: LinearLayout
    private lateinit var btnAddBirdSighting: Button

    // database helper
    private lateinit var dbHelper: DatabaseHelper

    // current trip
    private var currentTrip: Trip? = null
    private var tripId: Int = -1

    // bird sightings
    private var birdSightings = mutableListOf<BirdSighting>()
    private lateinit var birdAdapter: BirdSightingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_details)

        // initialize database
        dbHelper = DatabaseHelper(this)

        // get trip id from intent
        tripId = intent.getIntExtra("TRIP_ID", -1)

        // initialize views
        toolbar = findViewById(R.id.toolbar)
        tvTripName = findViewById(R.id.tvTripName)
        tvDate = findViewById(R.id.tvDate)
        tvTime = findViewById(R.id.tvTime)
        tvLocation = findViewById(R.id.tvLocation)
        tvDuration = findViewById(R.id.tvDuration)
        tvDescription = findViewById(R.id.tvDescription)
        btnEditTrip = findViewById(R.id.btnEditTrip)
        btnDeleteTrip = findViewById(R.id.btnDeleteTrip)
        recyclerViewBirds = findViewById(R.id.recyclerViewBirds)
        emptyStateBirds = findViewById(R.id.emptyStateBirds)
        btnAddBirdSighting = findViewById(R.id.btnAddBirdSighting)

        // setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // setup recyclerview
        recyclerViewBirds.layoutManager = LinearLayoutManager(this)

        // load trip details
        loadTripDetails()

        // setup button listeners
        btnEditTrip.setOnClickListener {
            // TODO: implement edit trip functionality
            Toast.makeText(this, "Edit trip feature coming soon", Toast.LENGTH_SHORT).show()
        }

        btnDeleteTrip.setOnClickListener {
            showDeleteConfirmation()
        }

        btnAddBirdSighting.setOnClickListener {
            // navigate to add bird sighting activity
            val intent = Intent(this, AddBirdSightingActivity::class.java)
            intent.putExtra("TRIP_ID", tripId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // refresh bird sightings when coming back from add bird activity
        loadBirdSightings()
    }

    private fun loadTripDetails() {
        if (tripId == -1) {
            Toast.makeText(this, "Error loading trip", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // get trip from database
        currentTrip = dbHelper.getTripById(tripId)

        if (currentTrip == null) {
            Toast.makeText(this, "Trip not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // display trip details
        tvTripName.text = currentTrip!!.tripName
        tvDate.text = currentTrip!!.date
        tvTime.text = currentTrip!!.time
        tvLocation.text = currentTrip!!.location
        tvDuration.text = "${currentTrip!!.duration} hours"
        tvDescription.text = currentTrip!!.description
    }

    private fun loadBirdSightings() {
        // get bird sightings for this trip from storage
        birdSightings = BirdSightingStorage.getBirdSightingsByTripId(tripId).toMutableList()

        if (birdSightings.isEmpty()) {
            // show empty state
            emptyStateBirds.visibility = View.VISIBLE
            recyclerViewBirds.visibility = View.GONE
        } else {
            // show bird list
            emptyStateBirds.visibility = View.GONE
            recyclerViewBirds.visibility = View.VISIBLE

            // setup adapter
            birdAdapter = BirdSightingAdapter(birdSightings)
            recyclerViewBirds.adapter = birdAdapter
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_trip)
            .setMessage(R.string.delete_confirmation)
            .setPositiveButton(R.string.yes) { _, _ ->
                deleteTrip()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun deleteTrip() {
        // delete from database
        val result = dbHelper.deleteTrip(tripId)

        if (result > 0) {
            Toast.makeText(this, R.string.trip_deleted, Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to delete trip", Toast.LENGTH_SHORT).show()
        }
    }
}