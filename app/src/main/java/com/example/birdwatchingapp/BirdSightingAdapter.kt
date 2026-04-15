package com.example.birdwatchingapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class BirdSightingAdapter(
    private val birdSightings: List<BirdSighting>,
    private val onItemClick: ((BirdSighting) -> Unit)? = null,  // Optional click listener
    private val onEditClick: ((BirdSighting) -> Unit)? = null,  // Edit callback
    private val onDeleteClick: ((BirdSighting) -> Unit)? = null  // Delete callback
) : RecyclerView.Adapter<BirdSightingAdapter.BirdSightingViewHolder>() {

    // Changed from 'class' to 'inner class' to access onItemClick
    inner class BirdSightingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSpecies: TextView = itemView.findViewById(R.id.tvSpecies)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvComments: TextView = itemView.findViewById(R.id.tvComments)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)

        val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEditBird)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDeleteBird)

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

            // Edit button click
            btnEdit.setOnClickListener {
                onEditClick?.invoke(birdSighting)
            }

            //Delete button Click
            btnDelete.setOnClickListener {
                onDeleteClick?.invoke(birdSighting)
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

