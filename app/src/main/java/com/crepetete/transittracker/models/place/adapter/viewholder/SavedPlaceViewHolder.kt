package com.crepetete.transittracker.models.place.adapter.viewholder

import android.content.Context
import android.support.design.widget.FloatingActionButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.crepetete.transittracker.R
import com.crepetete.transittracker.models.place.PlaceData
import com.crepetete.transittracker.models.place.PlacesController

class SavedPlaceViewHolder(context: Context,
                           parent: ViewGroup,
                           private val mContainer: View?,
                           private val onDeleteListener: (position: Int) -> Unit)
    : PlaceViewHolder(context, LayoutInflater.from(context).inflate(R.layout.card_place, parent,
        false)) {
    override fun setOnClickListeners(view: View, place: PlaceData) {
        val position = this.adapterPosition
        with(view.findViewById<FloatingActionButton>(R.id.button_remove)) {
            setImageResource(R.drawable.ic_delete_24dp)
            setOnClickListener({
                PlacesController.deletePlace(mContext, place)
                onDeleteListener(position)
            })
        }

        with(view.findViewById<FloatingActionButton>(R.id.button_save)) {
            setImageResource(R.drawable.ic_add_gray_24dp)
            setOnClickListener({
                PlacesController.addPlace(place)
//                mContainer?.let { container ->
//                    Snackbar.make(container,
//                            "Successfully added ${place.name} to your current trip.",
//                            Snackbar.LENGTH_SHORT)
//                            .setAction("UNDO", {
//                                PlacesController.removePlace(place.id)
//                                Snackbar.make(container, "Action reverted.",
//                                        Snackbar.LENGTH_SHORT)
//                                        .show()
//                            })
//                            .show()
//                }
                Toast.makeText(mContext,
                        "Successfully added ${place.name} to your current trip.",
                        Toast.LENGTH_SHORT).show()
            })
        }
    }

}