package com.crepetete.transittracker.models.place.adapter.viewholder.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.crepetete.transittracker.R
import com.crepetete.transittracker.models.place.ParcelablePlace
import com.crepetete.transittracker.models.place.PlacesController
import com.crepetete.transittracker.models.place.PlacesListener
import com.crepetete.transittracker.models.place.adapter.viewholder.PlaceViewHolder
import com.crepetete.transittracker.views.fragments.place.PlacePickerFragment
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.Places


class PlacesAdapter(private val mContext: Context, private val mPlaces: List<ParcelablePlace> = listOf(),
                    private val mIsPrivateDataSet: Boolean = false) : PlacesListener,
        RecyclerView.Adapter<PlaceViewHolder>() {

    private var mGeoDataClient: GeoDataClient? = null

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        if (mGeoDataClient != null) {
            mGeoDataClient = null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        if (mGeoDataClient == null) {
            mGeoDataClient = Places.getGeoDataClient(parent.context)
        }

        return PlaceViewHolder(mContext, LayoutInflater.from(parent.context)
                .inflate(R.layout.card_place, parent, false), mIsPrivateDataSet)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.setPlace(mPlaces[position], mGeoDataClient)
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

    override fun onPlaceRemoved(removedPosition: Int) {
        notifyItemRemoved(removedPosition)
    }

    fun onResume() {
        PlacesController.addListener(this)
    }

    fun onStop() {
        PlacesController.removeListener(getListenerTag())
    }
}