package com.crepetete.transittracker.models.place.adapter.viewholder

import android.animation.LayoutTransition
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.crepetete.transittracker.R
import com.crepetete.transittracker.models.place.ParcelablePlace
import com.crepetete.transittracker.models.place.PlacesController
import com.google.android.gms.location.places.GeoDataClient


class PlaceViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    private var mPlaceId: String = ""

    private val mTitleTextView by lazy { view.findViewById<TextView>(R.id.title) }
    private val mDescTextView by lazy { view.findViewById<TextView>(R.id.description) }
    private val mButtonRemove by lazy { view.findViewById<Button>(R.id.button_remove) }
    private val mImageBanner: ImageView = view.findViewById(R.id.banner)

    fun setPlace(place: ParcelablePlace, geoDataClient: GeoDataClient?) {
        mPlaceId = place.id

        mTitleTextView.text = place.name
        mDescTextView.text = place.address
        mButtonRemove.setOnClickListener({
            PlacesController.removePlace(place.id)
        })

        if (place.getImage() != null) {
            mImageBanner.setImageBitmap(place.getImage())
            mImageBanner.visibility = View.VISIBLE
        } else if (geoDataClient != null && !mPlaceId.isBlank()) {
            mImageBanner.visibility = View.GONE

            val v = view.findViewById<ViewGroup>(R.id.card_content)
            v.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

            PlacesController.loadImageForPlace(mPlaceId, geoDataClient)
        }
    }
}