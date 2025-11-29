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

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvTripName: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvDescription: TextView
    private lateinit var btnEdit: MaterialButton
    private lateinit var btnDelete: MaterialButton
    private lateinit var recyclerViewBirds: RecyclerView
    private lateinit var emptyBirdsState: LinearLayout
    private lateinit var fabAddBird: ExtendedFloatingActionButton

    private lateinit var dbHelper: DatabaseHelper
    private var tripId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_details)

        // get trip id from intent
        tripId = intent.getIntExtra("TRIP_ID", -1)

        if (tripId == -1) {
            Toast.makeText(this, "Error loading trip", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // initialize database
        dbHelper = DatabaseHelper(this)

        // initialize views
        initViews()

        // setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // setup button clicks
        setupClickListeners()

        // load trip data
        loadTripData()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        tvTripName = findViewById(R.id.tvTripName)
        tvDate = findViewById(R.id.tvDate)
        tvTime = findViewById(R.id.tvTime)
        tvLocation = findViewById(R.id.tvLocation)
        tvDuration = findViewById(R.id.tvDuration)
        tvDescription = findViewById(R.id.tvDescription)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)
        recyclerViewBirds = findViewById(R.id.recyclerViewBirds)
        emptyBirdsState = findViewById(R.id.emptyBirdsState)
        fabAddBird = findViewById(R.id.fabAddBird)

        // setup recyclerview
        recyclerViewBirds.layoutManager = LinearLayoutManager(this)
    }

    private fun setupClickListeners() {
        // edit button
        btnEdit.setOnClickListener {
            editTrip()
        }

        // delete button
        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }

        // fab for adding birds
        fabAddBird.setOnClickListener {
            val intent = Intent(this, AddBirdSightingActivity::class.java)
            intent.putExtra("TRIP_ID", tripId)
            startActivity(intent)
        }
    }

    private fun loadTripData() {
        val trip = dbHelper.getTripById(tripId)

        if (trip != null) {
            tvTripName.text = trip.tripName
            tvDate.text = trip.date
            tvTime.text = trip.time
            tvLocation.text = trip.location
            tvDuration.text = getString(R.string.hours_format, trip.duration)
            tvDescription.text = trip.description
        } else {
            Toast.makeText(this, "Trip not found", Toast.LENGTH_SHORT).show()
            finish()
        }

        // for now show empty state for birds
        // this will be updated when bird sighting feature is implemented
        recyclerViewBirds.visibility = View.GONE
        emptyBirdsState.visibility = View.VISIBLE
    }

    private fun editTrip() {
        val intent = Intent(this, AddTripActivity::class.java)
        intent.putExtra("TRIP_ID", tripId)
        startActivity(intent)
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_trip)
            .setMessage(R.string.delete_confirm_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteTrip()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteTrip() {
        val result = dbHelper.deleteTrip(tripId)

        if (result > 0) {
            Toast.makeText(this, R.string.trip_deleted, Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // reload data when returning from edit screen
        if (tripId != -1) {
            loadTripData()
        }
    }
}