package com.crepetete.transittracker.models.place.adapter.viewholder

import android.content.Context
import android.support.design.widget.FloatingActionButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.crepetete.transittracker.R
import com.crepetete.transittracker.models.place.PlaceData
import com.crepetete.transittracker.models.place.PlacesController

class ListPlaceViewHolder(context: Context,
                          parent: ViewGroup,
                          private val onDeleteListener: (position: Int) -> Unit)
    : PlaceViewHolder(context, LayoutInflater.from(context).inflate(R.layout.card_place,
        parent, false)) {
    override fun setOnClickListeners(view: View, place: PlaceData) {
        with(view.findViewById<FloatingActionButton>(R.id.button_remove)) {
            setImageResource(R.drawable.ic_clear_24dp)
            setOnClickListener({
                PlacesController.removePlace(place.id)
                onDeleteListener(adapterPosition)
            })
        }


        with(view.findViewById<FloatingActionButton>(R.id.button_save)) {
            setImageResource(R.drawable.ic_save_24dp)
            setOnClickListener({
                PlacesController.insertPlace(mContext, place)
            })
        }
    }
}