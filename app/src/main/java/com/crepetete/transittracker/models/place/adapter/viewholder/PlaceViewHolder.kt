package com.crepetete.transittracker.models.place.adapter.viewholder

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.crepetete.transittracker.R
import com.crepetete.transittracker.models.place.PlaceData
import com.crepetete.transittracker.models.place.PlacesController
import com.google.android.gms.location.places.GeoDataClient


abstract class PlaceViewHolder(
        protected val mContext: Context,
        private val mView: View)
    : RecyclerView.ViewHolder(mView) {

    private val mTitleTextView by lazy { mView.findViewById<TextView>(R.id.title) }
    private val mDescTextView by lazy { mView.findViewById<TextView>(R.id.description) }
    private val mImageBanner by lazy { mView.findViewById<ImageView>(R.id.banner) }

    fun setPlace(place: PlaceData, geoDataClient: GeoDataClient?) {
        mTitleTextView.text = place.name
        mDescTextView.text = place.address

        setOnClickListeners(mView, place)

        val image = place.getBitmap()
        if (image != null) {
            with(mImageBanner) {
                setImageBitmap(image)
                visibility = View.VISIBLE
            }
        } else {
            geoDataClient?.let {
                mImageBanner.visibility = View.GONE
                PlacesController.loadImageForPlace(place.id, it)
            }
        }
    }

    abstract fun setOnClickListeners(view: View, place: PlaceData)
}