package com.example.birdwatchingapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class BirdSightingAdapter(
    private val birdSightings: List<BirdSighting>,
    private val onItemClick: ((BirdSighting) -> Unit)? = null  // ADDED: Optional click listener
) : RecyclerView.Adapter<BirdSightingAdapter.BirdSightingViewHolder>() {

    // CHANGED: Changed from 'class' to 'inner class' to access onItemClick
    inner class BirdSightingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSpecies: TextView = itemView.findViewById(R.id.tvSpecies)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvComments: TextView = itemView.findViewById(R.id.tvComments)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)

        // ADDED: bind() method to handle click listener
        fun bind(birdSighting: BirdSighting) {
            tvSpecies.text = birdSighting.species
            tvQuantity.text = "Quantity: ${birdSighting.quantity}"

            // Show comments only if they exist
            if (birdSighting.comments.isNotEmpty()) {
                tvComments.text = birdSighting.comments
                tvComments.visibility = View.VISIBLE
            } else {
                tvComments.visibility = View.GONE
            }

            // Format and display timestamp
            val date = Date(birdSighting.timestamp)
            val formatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            tvTimestamp.text = formatter.format(date)

            // Click listener with safe call
            itemView.setOnClickListener {
                onItemClick?.invoke(birdSighting)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirdSightingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bird_sighting_card, parent, false)
        return BirdSightingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BirdSightingViewHolder, position: Int) {
        // Now calls bind() method instead of setting values directly
        holder.bind(birdSightings[position])
    }

    override fun getItemCount() = birdSightings.size
}