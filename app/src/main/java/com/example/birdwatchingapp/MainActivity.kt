package com.example.birdwatchingapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var fabAdd: FloatingActionButton
    private lateinit var tvTripsCount: TextView
    private lateinit var tvBirdsCount: TextView
    private lateinit var tvHoursCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initialize views
        fabAdd = findViewById(R.id.fabAdd)
        tvTripsCount = findViewById(R.id.tvTripsCount)
        tvBirdsCount = findViewById(R.id.tvBirdsCount)
        tvHoursCount = findViewById(R.id.tvHoursCount)

        // set initial values
        tvTripsCount.text = "0"
        tvBirdsCount.text = "0"
        tvHoursCount.text = "0"

        // fab click
        fabAdd.setOnClickListener {
            // not implemented yet
        }
    }
}