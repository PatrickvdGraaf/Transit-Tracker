package com.crepetete.transittracker.models.place.adapter.viewholder.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.crepetete.transittracker.models.place.PlaceData
import com.crepetete.transittracker.models.place.PlacesController
import com.crepetete.transittracker.models.place.PlacesListener
import com.crepetete.transittracker.models.place.adapter.viewholder.ListPlaceViewHolder
import com.crepetete.transittracker.models.place.adapter.viewholder.PlaceViewHolder
import com.crepetete.transittracker.models.place.adapter.viewholder.SavedPlaceViewHolder
import com.crepetete.transittracker.views.fragments.placelist.PlacePickerFragment
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.Places

class PlacesAdapter(private val mContext: Context,
                    private var mPlaces: MutableList<PlaceData> = mutableListOf(),
                    private val mContainer: View? = null,
                    private val isPrivateDataSet: Boolean = false) : PlacesListener,
        RecyclerView.Adapter<PlaceViewHolder>() {

    private var mGeoDataClient: GeoDataClient? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        if (mGeoDataClient == null) {
            mGeoDataClient = Places.getGeoDataClient(mContext)
        }

        return if (isPrivateDataSet) {
            SavedPlaceViewHolder(mContext, parent, mContainer, { position ->
                mPlaces.removeAt(position)
                onPlaceRemoved(position)
            })
        } else {
            ListPlaceViewHolder(mContext, parent, { position ->
                mPlaces.removeAt(position)
                onPlaceRemoved(position)
            })
        }
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.setPlace(mPlaces[position], mGeoDataClient)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mGeoDataClient = null
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
        if (!isPrivateDataSet) {
            mPlaces = PlacesController.getPlaces().toMutableList()
        }
    }

    fun onStop() {
        PlacesController.removeListener(getListenerTag())
    }
}