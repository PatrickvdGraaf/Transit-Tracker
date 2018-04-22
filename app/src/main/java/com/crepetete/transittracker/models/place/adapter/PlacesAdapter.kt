package com.crepetete.transittracker.models.place.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.crepetete.transittracker.R
import com.crepetete.transittracker.models.place.ParcelablePlace
import com.crepetete.transittracker.models.place.PlacesController
import com.crepetete.transittracker.models.place.PlacesListener
import com.crepetete.transittracker.models.place.adapter.viewholder.PlaceViewHolder
import com.crepetete.transittracker.views.fragments.place.PlacePickerFragment

class PlacesAdapter : PlacesListener, RecyclerView.Adapter<PlaceViewHolder>() {
    private val mPlaces: List<ParcelablePlace> = PlacesController.getPlaces()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        return PlaceViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.card_place, parent, false))
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.setPlace(mPlaces.get(position))
    }

    override fun getItemCount(): Int {
        return mPlaces.size
    }

    override fun getListenerTag(): String {
        return PlacePickerFragment.TAG
    }

    override fun onPlacesChanged(updatedPosition: Int) {
        notifyItemChanged(updatedPosition)
    }

    fun onResume() {
        PlacesController.addListener(this)
    }

    fun onStop() {
        PlacesController.removeListener(getListenerTag())
    }
}