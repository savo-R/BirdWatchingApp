package com.example.birdwatchingapp

data class Trip(
    val id: Int,
    val tripName: String,
    val date: String,
    val time: String,
    val location: String,
    val duration: String,
    val description: String
)