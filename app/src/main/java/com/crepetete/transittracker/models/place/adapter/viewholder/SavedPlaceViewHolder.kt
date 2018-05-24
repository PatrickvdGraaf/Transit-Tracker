package com.crepetete.transittracker.models.place.adapter.viewholder

import android.content.Context
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
        with(view.findViewById<Button>(R.id.button_remove)) {
            text = "Delete"
            setOnClickListener({
                PlacesController.deletePlace(mContext, place)
                onDeleteListener(position)
            })
        }

        with(view.findViewById<Button>(R.id.button_save)) {
            text = "Add"
            setOnClickListener({
                PlacesController.addPlace(place)
                mContainer?.let { container ->
                    Snackbar.make(container,
                            "Successfully added ${place.name} to your current trip.",
                            Snackbar.LENGTH_SHORT)
                            .setAction("UNDO", {
                                PlacesController.removePlace(place.id)
                                Snackbar.make(container, "Action reverted.",
                                        Snackbar.LENGTH_SHORT)
                                        .show()
                            })
                            .show()
                }
            })
        }
    }

}