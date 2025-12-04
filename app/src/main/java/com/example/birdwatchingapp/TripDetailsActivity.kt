package com.example.birdwatchingapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class TripDetailsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var tripId: Int = -1

    // Bird sighting variables
    private var birdSightingsList = mutableListOf<BirdSighting>()
    private var adapter: BirdSightingAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_details)

        // step 1: Get the trip ID from the intent
        // The previous activitv(MainActivity)
        // passes this to tell us which trip to display
        tripId = intent.getIntExtra("TRIP_ID", -1)

        // Step 2: validate trip ID
        // If tripID is -1, something is wrong- show error & close
        if (tripId == -1) {
            Toast.makeText(this, "Error loading trip", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Step 3: initialize the database helper
        // This allows us to query the SQlite database
        // for trip and bird data
        dbHelper = DatabaseHelper(this)

        // Step 4: setup toolbar(back button,title)
        try {
            val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationOnClickListener {
                finish()// close activity and return to previous screen
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //step 5: setup RecycleView once at startup
        // prevents issues with recreating the layout, manager
        //multiple times
        setupRecyclerView()

        // Step 6:setup button click listeners
        //(edit, delete, add bird)
        // Setup buttons
        setupClickListeners()

        // step 7: load trip details from
        // database & display them
        loadTripData()

        // Step 8:
        // Load bird sightings
        // from database and display them
        loadBirdSightings()
    }

    // Scroll and see all birds
    private fun setupRecyclerView() {
        try {
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewBirds)
            recyclerView?.apply {
                // Set LinearLayoutManager to display items in a vertical list
                layoutManager = LinearLayoutManager(this@TripDetailsActivity)

                //disable nested scrolling
                // This allows the parent scrollview to handle scrolling
                // for all items
                // without this, RecyclerView only shows 1-2 items
                // and you can't scroll to see more
                isNestedScrollingEnabled = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun setupClickListeners() {
        // Edit button- allows user to modify trip details
        try {
            findViewById<MaterialButton>(R.id.btnEdit)?.setOnClickListener {
                editTrip()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Delete button- removes trip from database
        try {
            findViewById<MaterialButton>(R.id.btnDelete)?.setOnClickListener {
                showDeleteConfirmation()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // FAB for adding birds
        //- to add new bird sightings
        try {
            findViewById<ExtendedFloatingActionButton>(R.id.fabAddBird)?.setOnClickListener {
                // create intent to open
                //AddbirdSightingActivity
                val intent = Intent(this, AddBirdSightingActivity::class.java)

                //Pass trip ID so the bird gets
                // associated with this trip
                intent.putExtra("TRIP_ID", tripId)

                // start the activity
                //when user saves and returns
                //onResume() will be called automatically
                startActivity(intent)

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun loadTripData() {
        //query database for trip with
        // this ID
        val trip = dbHelper.getTripById(tripId)

        if (trip != null) {
            try {
                // display trip detials in UI
                findViewById<TextView>(R.id.tvTripDate)?.text = trip.date
                findViewById<TextView>(R.id.tvTripTime)?.text = trip.time
                findViewById<TextView>(R.id.tvTripLocation)?.text = trip.location
                findViewById<TextView>(R.id.tvTripDuration)?.text = "${trip.duration} hours"
                findViewById<TextView>(R.id.tvTripDescription)?.text = trip.description
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error displaying trip details", Toast.LENGTH_SHORT).show()
            }
        } else {
            //trip not found in database
            // show error and close activity
            Toast.makeText(this, "Trip not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadBirdSightings() {
        if (tripId == -1) {
            return
        }

        try {
            //STEP 1: Query database for all bird sightings for THIS specific trip
            //getBirdSightingsByTripId() returns birds where bird_sightings.trip_id = this tripId
            //Results are ordered by timestamp DESC (newest first)
            birdSightingsList = dbHelper.getBirdSightingsByTripId(tripId).toMutableList()
            //Step 2: get references to UI elements
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewBirds)
            val emptyState = findViewById<LinearLayout>(R.id.emptyStateBirds)
            val countTextView = findViewById<TextView>(R.id.tvBirdSightingsCount)

            //step 3: update the count display
            //shows "bird Sightings","Bird sightings(1)" etc
            countTextView?.text = "Bird Sightings (${birdSightingsList.size})"


            // STEP 4: Show either empty state OR bird list depending on what we have
            if (recyclerView != null) {
                if (birdSightingsList.isEmpty()) {
                    // NO BIRDS: Show empty state message
                    // This is the "No Birds Spotted Yet" screen with the bird emoji
                    emptyState?.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    // BIRDS EXIST: Hide empty state and show RecyclerView
                    emptyState?.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    // create adapter with bird data
                    adapter= BirdSightingAdapter(birdSightingsList)
                    recyclerView.adapter = adapter
                }
            }
        } catch (e: Exception) {
            // If anything goes wrong, show error message
            e.printStackTrace()
            Toast.makeText(this, "Error loading bird sightings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


// open AddTripActivity in edit mode
// passes the trip ID so the activity
// knows which trip to edit

    private fun editTrip() {
        val intent = Intent(this, AddTripActivity::class.java)
        intent.putExtra("TRIP_ID", tripId)
        startActivity(intent)
    }

// show confirmation dialog before deleting the trip
// to prevent accidental deletions
    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Trip")
            .setMessage("Are you sure you want to delete this trip?")
            .setPositiveButton("Delete") { _, _ ->
                deleteTrip()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

// delete trip from database
// uses cascade delete so all birds sightings
// for this trip are deleted
    private fun deleteTrip() {
        val result = dbHelper.deleteTrip(tripId)

        if (result > 0) {
            Toast.makeText(this, "Trip deleted", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to delete trip", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload data when returning from edit screen or adding bird
        if (tripId != -1) {
            loadTripData()
            loadBirdSightings()
        }
    }
}