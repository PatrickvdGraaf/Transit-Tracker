package com.crepetete.transittracker.models.place.adapter.viewholder

import android.animation.LayoutTransition
import android.content.Context
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


class PlaceViewHolder(private val mContext: Context, private val view: View,
                      private val privateDataSet: Boolean = false) : RecyclerView.ViewHolder(view) {

    private var mPlaceId: String = ""

    private val mTitleTextView by lazy { view.findViewById<TextView>(R.id.title) }
    private val mDescTextView by lazy { view.findViewById<TextView>(R.id.description) }
    private val mImageBanner by lazy { view.findViewById<ImageView>(R.id.banner) }

    fun setPlace(place: ParcelablePlace, geoDataClient: GeoDataClient?) {
        mPlaceId = place.id

        mTitleTextView.text = place.name
        mDescTextView.text = place.address

        if (privateDataSet) {
            val button1 = view.findViewById<Button>(R.id.button_remove)
            button1.text = "Delete"
            button1.setOnClickListener({
                PlacesController.deletePlace(mContext, place.id)
            })

            val button2 = view.findViewById<Button>(R.id.button_save)
            button2.text = "Add"
            button1.setOnClickListener({
                PlacesController.addPlace(place)
            })
        } else {
            view.findViewById<Button>(R.id.button_remove).setOnClickListener({
                PlacesController.removePlace(place.id)
            })

            view.findViewById<Button>(R.id.button_save).setOnClickListener({
                PlacesController.savePlace(mContext, place)
            })
        }

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