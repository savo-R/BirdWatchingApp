package com.example.birdwatchingapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var fabAdd: FloatingActionButton
    private lateinit var tvTripsCount: TextView
    private lateinit var tvBirdsCount: TextView
    private lateinit var tvHoursCount: TextView

    // database helper
    private lateinit var dbHelper: DatabaseHelper

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
    }

    private fun loadStats() {
        // get trip count from database
        val tripCount = dbHelper.getTripCount()

        // update UI
        tvTripsCount.text = tripCount.toString()
        tvBirdsCount.text = "0"
        tvHoursCount.text = "0"
    }
}