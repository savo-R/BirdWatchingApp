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

        // Comments are optional, no validation needed

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

        // Create bird sighting object with trip ID
        val birdSighting = BirdSighting(
            tripId = tripId,
            species = species,
            quantity = quantity,
            comments = comments,
            timestamp = System.currentTimeMillis()
        )

        // Add to in-memory storage
        BirdSightingStorage.addBirdSighting(birdSighting)

        Toast.makeText(
            this,
            "Bird sighting added!\n$quantity $species spotted",
            Toast.LENGTH_LONG
        ).show()

        // TODO: When database is ready, save to database instead of memory
        // database.insertBirdSighting(birdSighting)

        finish()
    }
}

// Data class for bird sighting (now includes tripId)
data class BirdSighting(
    val tripId: Int,  // Links bird sighting to a specific trip
    val species: String,
    val quantity: Int,
    val comments: String,
    val timestamp: Long
)

// Temporary in-memory storage (replace with database later)
object BirdSightingStorage {
    private val birdSightings = mutableListOf<BirdSighting>()

    fun addBirdSighting(sighting: BirdSighting) {
        birdSightings.add(sighting)
    }

    fun getAllBirdSightings(): List<BirdSighting> {
        return birdSightings.toList()
    }

    fun getBirdSightingsByTripId(tripId: Int): List<BirdSighting> {
        return birdSightings.filter { it.tripId == tripId }
    }

    fun clearAll() {
        birdSightings.clear()
    }

    fun getBirdSightingCount(): Int {
        return birdSightings.size
    }

    fun getTotalBirdCount(): Int {
        return birdSightings.sumOf { it.quantity }
    }
}