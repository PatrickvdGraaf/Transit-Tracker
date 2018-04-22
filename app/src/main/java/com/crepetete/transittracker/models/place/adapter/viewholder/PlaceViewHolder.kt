package com.crepetete.transittracker.models.place.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.crepetete.transittracker.R
import com.crepetete.transittracker.models.place.ParcelablePlace
import com.crepetete.transittracker.models.place.PlacesController

class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val mTitleTextView: TextView = view.findViewById(R.id.title)
    private val mDescTextView: TextView = view.findViewById(R.id.description)
    private val mButtonRemove: Button = view.findViewById(R.id.button_remove)

    fun setPlace(place: ParcelablePlace) {
        mTitleTextView.text = place.name
        mDescTextView.text = place.address
        mButtonRemove.setOnClickListener({
            PlacesController.removePlace(place.id)
        })
    }
}