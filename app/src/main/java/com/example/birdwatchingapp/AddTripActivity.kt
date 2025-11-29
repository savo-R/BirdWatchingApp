package com.example.birdwatchingapp

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*

class AddTripActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var etTripName: EditText
    private lateinit var etDate: EditText
    private lateinit var btnGetLocation: ImageButton
    private lateinit var etTime: EditText
    private lateinit var etLocation: EditText
    private lateinit var etDuration: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button

    // database helper
    private lateinit var dbHelper: DatabaseHelper

    // Native Android Location Manager
    private lateinit var locationManager: LocationManager
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    // Calendar for storing selected date and time
    private val calendar = Calendar.getInstance()

    // edit mode variables
    private var isEditMode = false
    private var editTripId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_trip)

        // initialize database
        dbHelper = DatabaseHelper(this)

        // initialise location manager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // initialize views
        toolbar = findViewById(R.id.toolbar)
        etTripName = findViewById(R.id.etTripName)
        etDate = findViewById(R.id.etDate)
        btnGetLocation = findViewById(R.id.btnGetLocation)
        etTime = findViewById(R.id.etTime)
        etLocation = findViewById(R.id.etLocation)
        etDuration = findViewById(R.id.etDuration)
        etDescription = findViewById(R.id.etDescription)
        btnSave = findViewById(R.id.btnSave)

        // check if we are in edit mode
        editTripId = intent.getIntExtra("TRIP_ID", -1)
        isEditMode = editTripId != -1

        // setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // setup for edit mode or add mode
        if (isEditMode) {
            supportActionBar?.title = getString(R.string.edit_trip)
            btnSave.text = getString(R.string.update_trip)
            loadTripData()
        }

        // Setup date and time pickers
        setupDateTimePickers()

        // location button click listener
        btnGetLocation.setOnClickListener {
            requestLocationPermission()
        }

        // save button click
        btnSave.setOnClickListener {
            if (validateInputs()) {
                if (isEditMode) {
                    updateTripDetails()
                } else {
                    saveTripDetails()
                }
            }
        }
    }

    private fun loadTripData() {
        val trip = dbHelper.getTripById(editTripId)

        if (trip != null) {
            etTripName.setText(trip.tripName)
            etDate.setText(trip.date)
            etTime.setText(trip.time)
            etLocation.setText(trip.location)
            etDuration.setText(trip.duration)
            etDescription.setText(trip.description)
        } else {
            Toast.makeText(this, "Error loading trip data", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupDateTimePickers() {
        etDate.setOnClickListener { showDatePicker() }
        etTime.setOnClickListener { showTimePicker() }

        // Make fields non-editable by keyboard
        etDate.isFocusable = false
        etDate.isClickable = true
        etTime.isFocusable = false
        etTime.isClickable = true
    }

    // ---- LOCATION PERMISSION & FETCHING ----

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted, get location
                getCurrentLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(
                    this,
                    "Location permission needed to get your current location",
                    Toast.LENGTH_LONG
                ).show()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show()
                    getCurrentLocation()
                } else {
                    Toast.makeText(
                        this,
                        "Permission Denied! You can type your location manually.",
                        Toast.LENGTH_LONG
                    ).show()
                    etLocation.requestFocus()
                }
            }
        }
    }

    private fun getCurrentLocation() {
        // Check permissions again
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Show loading state
        btnGetLocation.isEnabled = false
        Toast.makeText(this, "Getting your location...", Toast.LENGTH_SHORT).show()

        // Try to get last known location first (faster)
        val lastKnownLocation = getLastKnownLocation()

        if (lastKnownLocation != null) {
            // Use last known location
            btnGetLocation.isEnabled = true
            getAddressFromLocation(lastKnownLocation)
        } else {
            // Request fresh location update
            requestSingleLocationUpdate()
        }
    }

    // Check last cached location stored from previous location requests
    private fun getLastKnownLocation(): Location? {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        // Try GPS provider first (most accurate)
        val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (gpsLocation != null) return gpsLocation

        // Fall back to network provider
        val networkLocation =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (networkLocation != null) return networkLocation

        return null
    }

    private fun requestSingleLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            btnGetLocation.isEnabled = true
            return
        }

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationManager.removeUpdates(this)
                btnGetLocation.isEnabled = true
                getAddressFromLocation(location)
            }

            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {
                btnGetLocation.isEnabled = true
                Toast.makeText(
                    this@AddTripActivity,
                    "Please enable GPS in settings. You can type location manually.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        try {
            // Try GPS first
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )

                // Set timeout - if no location in 10 seconds, try network
                android.os.Handler(mainLooper).postDelayed({
                    locationManager.removeUpdates(locationListener)
                    tryNetworkProvider(locationListener)
                }, 10000)

            } else {
                // GPS disabled, try network
                tryNetworkProvider(locationListener)
            }
        } catch (e: Exception) {
            btnGetLocation.isEnabled = true
            Toast.makeText(
                this,
                "Error getting location: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun tryNetworkProvider(locationListener: LocationListener) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            btnGetLocation.isEnabled = true
            return
        }

        try {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )

                // Set timeout
                android.os.Handler(mainLooper).postDelayed({
                    locationManager.removeUpdates(locationListener)
                    btnGetLocation.isEnabled = true
                    Toast.makeText(
                        this,
                        "Unable to get location. Please type manually.",
                        Toast.LENGTH_LONG
                    ).show()
                }, 10000)
            } else {
                btnGetLocation.isEnabled = true
                Toast.makeText(
                    this,
                    "Location services disabled. Please type manually.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            btnGetLocation.isEnabled = true
            Toast.makeText(
                this,
                "Unable to get location. Please type manually.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getAddressFromLocation(location: Location) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]

                // Build a readable address
                val addressText = buildString {
                    address.featureName?.let { append("$it, ") }
                    address.locality?.let { append("$it, ") }
                    address.adminArea?.let { append("$it, ") }
                    address.countryName?.let { append(it) }
                }.trimEnd(',', ' ')

                if (addressText.isNotEmpty()) {
                    etLocation.setText(addressText)
                    Toast.makeText(this, "Location obtained!", Toast.LENGTH_SHORT).show()
                } else {
                    val coordinates = "${location.latitude}, ${location.longitude}"
                    etLocation.setText(coordinates)
                    Toast.makeText(this, "Location set to coordinates", Toast.LENGTH_SHORT).show()
                }
            } else {
                val coordinates = "${location.latitude}, ${location.longitude}"
                etLocation.setText(coordinates)
                Toast.makeText(this, "Location set to coordinates", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            val coordinates = "${location.latitude}, ${location.longitude}"
            etLocation.setText(coordinates)
            Toast.makeText(this, "Location set to coordinates", Toast.LENGTH_SHORT).show()
        }
    }

    // ---- DATE / TIME PICKERS ----

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateField()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                updateTimeField()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun updateDateField() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        etDate.setText(dateFormat.format(calendar.time))
    }

    private fun updateTimeField() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        etTime.setText(timeFormat.format(calendar.time))
    }

    // ---- VALIDATION & SAVE TO DB ----

    private fun validateInputs(): Boolean {
        var isValid = true

        if (etTripName.text.toString().trim().isEmpty()) {
            etTripName.error = "Trip name is required"
            isValid = false
        }

        if (etDate.text.toString().trim().isEmpty()) {
            etDate.error = "Date is required"
            isValid = false
        }

        if (etTime.text.toString().trim().isEmpty()) {
            etTime.error = "Time is required"
            isValid = false
        }

        if (etLocation.text.toString().trim().isEmpty()) {
            etLocation.error = "Location is required"
            isValid = false
        }

        if (etDuration.text.toString().trim().isEmpty()) {
            etDuration.error = "Duration is required"
            isValid = false
        } else {
            val duration = etDuration.text.toString().toFloatOrNull()
            if (duration == null || duration <= 0) {
                etDuration.error = "Please enter a valid duration"
                isValid = false
            }
        }

        if (etDescription.text.toString().trim().isEmpty()) {
            etDescription.error = "Description is required"
            isValid = false
        }

        return isValid
    }

    private fun saveTripDetails() {
        val tripName = etTripName.text.toString().trim()
        val date = etDate.text.toString().trim()
        val time = etTime.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val duration = etDuration.text.toString().trim()
        val description = etDescription.text.toString().trim()

        // save to database
        val result = dbHelper.insertTrip(tripName, date, time, location, duration, description)

        if (result > 0) {
            Toast.makeText(this, R.string.trip_saved, Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTripDetails() {
        val tripName = etTripName.text.toString().trim()
        val date = etDate.text.toString().trim()
        val time = etTime.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val duration = etDuration.text.toString().trim()
        val description = etDescription.text.toString().trim()

        // update in database
        val result = dbHelper.updateTrip(editTripId, tripName, date, time, location, duration, description)

        if (result > 0) {
            Toast.makeText(this, R.string.trip_updated, Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, R.string.update_failed, Toast.LENGTH_SHORT).show()
        }
    }
}