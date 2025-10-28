package com.example.birdwatchingapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*

class AddTripActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var etTripName: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var etLocation: EditText
    private lateinit var etDuration: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button

    // Calendar for storing selected date and time
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_trip)

        // initialize views
        toolbar = findViewById(R.id.toolbar)
        etTripName = findViewById(R.id.etTripName)
        etDate = findViewById(R.id.etDate)
        etTime = findViewById(R.id.etTime)
        etLocation = findViewById(R.id.etLocation)
        etDuration = findViewById(R.id.etDuration)
        etDescription = findViewById(R.id.etDescription)
        btnSave = findViewById(R.id.btnSave)

        // setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Setup date and time pickers
        setupDateTimePickers()

        // save button click
        btnSave.setOnClickListener {
            if (validateInputs()) {
                saveTripDetails()
            }
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

        // ✅ Validate Description
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
        val description = etDescription.text.toString().trim() // added

        // Show success message with description
        Toast.makeText(
            this,
            "Trip '$tripName' saved!\nDescription: $description",
            Toast.LENGTH_LONG
        ).show()

        // TODO: Save all database here

        finish()
    }
}
