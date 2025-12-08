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
    private lateinit var btnUpload: MaterialButton
    private lateinit var recyclerViewBirds: RecyclerView
    private lateinit var emptyBirdsState: LinearLayout
    private lateinit var fabAddBird: ExtendedFloatingActionButton

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var firebaseHelper: FirebaseHelper
    private var tripId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_details)

        tripId = intent.getIntExtra("TRIP_ID", -1)

        if (tripId == -1) {
            Toast.makeText(this, "Error loading trip", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        dbHelper = DatabaseHelper(this)
        firebaseHelper = FirebaseHelper(dbHelper)

        initViews()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        setupClickListeners()
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
        btnUpload = findViewById(R.id.btnUpload)
        recyclerViewBirds = findViewById(R.id.recyclerViewBirds)
        emptyBirdsState = findViewById(R.id.emptyBirdsState)
        fabAddBird = findViewById(R.id.fabAddBird)

        recyclerViewBirds.layoutManager = LinearLayoutManager(this)
    }

    private fun setupClickListeners() {
        btnEdit.setOnClickListener { editTrip() }
        btnDelete.setOnClickListener { showDeleteConfirmation() }
        btnUpload.setOnClickListener { uploadTrip() }

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
            return
        }

        // load bird sightings for this trip
        loadBirdSightings()
    }

    private fun loadBirdSightings() {
        val birdSightings = dbHelper.getBirdSightingsByTripId(tripId)

        if (birdSightings.isEmpty()) {
            recyclerViewBirds.visibility = View.GONE
            emptyBirdsState.visibility = View.VISIBLE
        } else {
            recyclerViewBirds.visibility = View.VISIBLE
            emptyBirdsState.visibility = View.GONE

            // Pass callbacks for edit and delete
            val adapter = BirdSightingAdapter(
                birdSightings = birdSightings,
                onEditClick = { bird -> editBirdSighting(bird) },
                onDeleteClick = { bird -> deleteBirdSighting(bird) }
            )
            recyclerViewBirds.adapter = adapter
        }
    }

    private fun editBirdSighting(bird: BirdSighting) {
        // Navigate to AddBirdSightingActivity with bird data for editing
        val intent = Intent(this, AddBirdSightingActivity::class.java)
        intent.putExtra("TRIP_ID", tripId)
        intent.putExtra("BIRD_ID", bird.id)
        startActivity(intent)
    }

    private fun deleteBirdSighting(bird: BirdSighting) {
        AlertDialog.Builder(this)
            .setTitle("Delete Bird Sighting")
            .setMessage("Delete ${bird.species} sighting?")
            .setPositiveButton("Delete") { _, _ ->
                val result = dbHelper.deleteBirdSighting(bird.id)
                if (result > 0) {
                    Toast.makeText(this, "Bird sighting deleted", Toast.LENGTH_SHORT).show()
                    loadBirdSightings() // refresh list
                } else {
                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
            .setPositiveButton(R.string.delete) { _, _ -> deleteTrip() }
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

    private fun uploadTrip() {
        val trip = dbHelper.getTripById(tripId)

        if (trip == null) {
            Toast.makeText(this, "Trip not found", Toast.LENGTH_SHORT).show()
            return
        }

        btnUpload.isEnabled = false
        Toast.makeText(this, "Uploading trip...", Toast.LENGTH_SHORT).show()

        firebaseHelper.uploadTrip(
            trip = trip,
            onSuccess = {
                btnUpload.isEnabled = true
                Toast.makeText(this, "✓ Trip uploaded to Firebase!", Toast.LENGTH_LONG).show()
            },
            onFailure = { error ->
                btnUpload.isEnabled = true
                Toast.makeText(this, "✗ Upload failed: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    override fun onResume() {
        super.onResume()
        if (tripId != -1) {
            loadTripData()
        }
    }
}