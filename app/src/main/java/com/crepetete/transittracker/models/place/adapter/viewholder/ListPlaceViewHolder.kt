package com.crepetete.transittracker.models.place.adapter.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.crepetete.transittracker.R
import com.crepetete.transittracker.models.place.PlaceData
import com.crepetete.transittracker.models.place.PlacesController

class ListPlaceViewHolder(context: Context,
                          parent: ViewGroup,
                          private val onDeleteListener: (position: Int) -> Unit)
    : PlaceViewHolder(context, LayoutInflater.from(context).inflate(R.layout.card_place,
        parent, false)) {
    override fun setOnClickListeners(view: View, place: PlaceData) {
        view.findViewById<Button>(R.id.button_remove).setOnClickListener({
            PlacesController.removePlace(place.id)
            onDeleteListener(adapterPosition)
        })

        view.findViewById<Button>(R.id.button_save).setOnClickListener({
            PlacesController.insertPlace(mContext, place)
        })
    }
}