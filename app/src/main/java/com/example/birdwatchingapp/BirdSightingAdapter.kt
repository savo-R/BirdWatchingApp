package com.example.birdwatchingapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BirdSightingAdapter(
    private val birdSightings: List<BirdSighting>
) : RecyclerView.Adapter<BirdSightingAdapter.BirdSightingViewHolder>() {

    class BirdSightingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSpecies: TextView = view.findViewById(R.id.tvSpecies)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val tvComments: TextView = view.findViewById(R.id.tvComments)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirdSightingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bird_sighting_card, parent, false)
        return BirdSightingViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BirdSightingViewHolder, position: Int) {
        val birdSighting = birdSightings[position]

        holder.tvSpecies.text = birdSighting.species
        holder.tvQuantity.text = birdSighting.quantity.toString()

        // show comments only if they exist
        if (birdSighting.comments.isNotEmpty()) {
            holder.tvComments.text = birdSighting.comments
            holder.tvComments.visibility = View.VISIBLE
        } else {
            holder.tvComments.visibility = View.GONE
        }
    }

    override fun getItemCount() = birdSightings.size
}