package com.example.birdwatchingapp

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputLayout

class AddBirdSightingActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var spinnerSpecies: Spinner
    private lateinit var tilCustomSpecies: TextInputLayout  // Add this for the TextInputLayout wrapper
    private lateinit var etCustomSpecies: EditText
    private lateinit var etQuantity: EditText
    private lateinit var etComments: EditText
    private lateinit var btnAddBird: Button
    private lateinit var btnCancel: Button

    //database helper
    private lateinit var databaseHelper: DatabaseHelper

    private var tripId: Int = -1

    // Bird species list
    private val birdSpeciesList = mutableListOf(
        "Select Species",
        "Robin",
        "Eagle",
        "Sparrow",
        "Blue Jay",
        "Cardinal",
        "Hawk",
        "Owl",
        "Hummingbird",
        "Woodpecker",
        "Crow",
        "Pigeon",
        "Seagull",
        "Parrot",
        "Falcon",
        "Swallow",
        "Magpie",
        "Kingfisher",
        "Custom (Type your own)"  // This option lets users type their own bird
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bird_sighting)

        //Initial database helper
        databaseHelper = DatabaseHelper(this)


        // Get trip ID from intent
        tripId = intent.getIntExtra("TRIP_ID", -1)

        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        spinnerSpecies = findViewById(R.id.spinnerSpecies)
        tilCustomSpecies = findViewById(R.id.tilCustomSpecies)  // Initialize the TextInputLayout
        etCustomSpecies = findViewById(R.id.etCustomSpecies)
        etQuantity = findViewById(R.id.etQuantity)
        etComments = findViewById(R.id.etComments)
        btnAddBird = findViewById(R.id.btnAddBird)
        btnCancel = findViewById(R.id.btnCancel)

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Update toolbar title with trip info if available
        if (tripId != -1) {
            supportActionBar?.title = "Add Bird Sighting to Trip"
        }

        // Setup species spinner
        setupSpeciesSpinner()

        // Setup buttons
        btnAddBird.setOnClickListener {
            if (validateInputs()) {
                saveBirdSighting()
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun setupSpeciesSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            birdSpeciesList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSpecies.adapter = adapter

        // Show/hide custom species input based on selection
        spinnerSpecies.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (birdSpeciesList[position] == "Custom (Type your own)") {
                    // FIXED: Set visibility on the TextInputLayout wrapper, not just the EditText
                    tilCustomSpecies.visibility = View.VISIBLE
                    etCustomSpecies.requestFocus()
                } else {
                    tilCustomSpecies.visibility = View.GONE
                    etCustomSpecies.setText("")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                tilCustomSpecies.visibility = View.GONE
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate species
        val selectedSpecies = spinnerSpecies.selectedItem.toString()
        if (selectedSpecies == "Select Species") {
            Toast.makeText(this, "Please select a bird species", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Validate custom species if selected
        if (selectedSpecies == "Custom (Type your own)") {
            if (etCustomSpecies.text.toString().trim().isEmpty()) {
                etCustomSpecies.error = "Please enter bird species name"
                isValid = false
            }
        }

        // Validate quantity
        if (etQuantity.text.toString().trim().isEmpty()) {
            etQuantity.error = "Quantity is required"
            isValid = false
        } else {
            val quantity = etQuantity.text.toString().toIntOrNull()
            if (quantity == null || quantity <= 0) {
                etQuantity.error = "Please enter a valid quantity"
                isValid = false
            }
        }

        // Comments needs no validation.

        return isValid
    }

    private fun saveBirdSighting() {
        // Get species name (either from dropdown or custom input)
        val species = if (spinnerSpecies.selectedItem.toString() == "Custom (Type your own)") {
            etCustomSpecies.text.toString().trim()
        } else {
            spinnerSpecies.selectedItem.toString()
        }

        val quantity = etQuantity.text.toString().toInt()
        val comments = etComments.text.toString().trim()


        // Now saving to database
        val result = databaseHelper.insertBirdSighting(
            tripId = tripId,
            species = species,
            quantity = quantity,
            comments = comments,
            timestamp = System.currentTimeMillis()
        )

        if (result != -1L) {
            // Successfully saved
            Toast.makeText(
                this,
                "Bird sighting saved to database!\n$quantity $species spotted",
                Toast.LENGTH_LONG
            ).show()
            finish()
        } else {
            // Error saving
            Toast.makeText(
                this,
                "Error saving bird sighting. Please try again.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

// Updated BirdSighting data class with id field
data class BirdSighting(
    val id: Int = 0,          // Database ID (auto-generated)
    val tripId: Int,          // Links bird sighting to a specific trip
    val species: String,      // Bird species name
    val quantity: Int,        // Number of birds spotted
    val comments: String,     // Optional observation notes
    val timestamp: Long       // When the sighting was recorded
)