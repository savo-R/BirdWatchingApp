package com.example.birdwatchingapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TripAdapter(
    private val trips: List<Trip>,
    private val onTripClick: (Trip) -> Unit
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    class TripViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTripName: TextView = view.findViewById(R.id.tvTripName)
        val tvDateTime: TextView = view.findViewById(R.id.tvDateTime)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val btnViewDetails: Button = view.findViewById(R.id.btnViewDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip_card, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]

        holder.tvTripName.text = trip.tripName
        holder.tvDateTime.text = "${trip.date} - ${trip.time}"
        holder.tvLocation.text = trip.location

        holder.btnViewDetails.setOnClickListener {
            onTripClick(trip)
        }
    }

    override fun getItemCount() = trips.size
}