package com.crepetete.transittracker.models.place.adapter.viewholder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.crepetete.transittracker.R
import com.crepetete.transittracker.models.place.ParcelablePlace
import com.crepetete.transittracker.models.place.PlacesController
import com.gjiazhe.scrollparallaximageview.ScrollParallaxImageView
import com.gjiazhe.scrollparallaximageview.parallaxstyle.VerticalMovingStyle
import com.google.android.gms.location.places.GeoDataClient
import timber.log.Timber

class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private var mPlaceId: String = ""

    private val mTitleTextView by lazy { view.findViewById<TextView>(R.id.title) }
    private val mDescTextView by lazy { view.findViewById<TextView>(R.id.description) }
    private val mButtonRemove by lazy { view.findViewById<Button>(R.id.button_remove) }
    private val mImageBanner: ImageView = view.findViewById(R.id.banner)

    private var mImageVisibility = View.GONE

    fun setPlace(place: ParcelablePlace) {
        mPlaceId = place.id

        mTitleTextView.text = place.name
        mDescTextView.text = place.address
        mButtonRemove.setOnClickListener({
            PlacesController.removePlace(place.id)
        })

        if (mImageBanner is ScrollParallaxImageView) {
            mImageBanner.setParallaxStyles(VerticalMovingStyle())
        }
    }

    private fun setImageVisibility(visibility: Int) {
        mImageVisibility = visibility
        mImageBanner.visibility = visibility
    }

    fun setImage(geoDataClient: GeoDataClient?) {
        if (geoDataClient == null || mPlaceId.isBlank()) {
            return
        }

        geoDataClient.getPlacePhotos(mPlaceId).addOnCompleteListener { task ->
            // Get the PlacePhotoMetadataBuffer (metadata for all the photos).
            val photoMetadataBuffer = task.result.photoMetadata
            if (photoMetadataBuffer.count > 0) {
                // Get first photo in the list.
                val photoMetadata = photoMetadataBuffer[0]
                // Get the attribution text.
                // TODO show attribution
                val attribution = photoMetadata.attributions
                // Get a full-size bitmap for the photo
                geoDataClient.getPhoto(photoMetadata)
                        .addOnCompleteListener { photoResponseTask ->
                            if (photoResponseTask.isSuccessful) {
                                updateImageBanner(geoDataClient.applicationContext,
                                        photoResponseTask.result.bitmap)
                            } else {
                                Timber.d("")
                                setImageVisibility(View.GONE)
                            }
                        }
            } else {
                //TODO add map snippet
            }
        }
    }

    private fun updateImageBanner(context: Context, bitmap: Bitmap) {
        Glide.with(context)
                .load(bitmap)
                .listener(object : RequestListener<Drawable> {
                    override fun onResourceReady(resource: Drawable?,
                                                 model: Any?,
                                                 target: com.bumptech.glide.request.target.Target<Drawable>?,
                                                 dataSource: DataSource?,
                                                 isFirstResource: Boolean): Boolean {
                        setImageVisibility(View.VISIBLE)
                        return false
                    }

                    override fun onLoadFailed(e: GlideException?,
                                              model: Any?,
                                              target: com.bumptech.glide.request.target.Target<Drawable>?,
                                              isFirstResource: Boolean): Boolean {
                        setImageVisibility(View.GONE)
                        return false
                    }
                })
                .into(mImageBanner)
    }
}