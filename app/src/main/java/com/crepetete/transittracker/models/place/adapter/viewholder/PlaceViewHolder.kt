package com.crepetete.transittracker.models.place.adapter.viewholder

import android.animation.LayoutTransition
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.crepetete.transittracker.R
import com.crepetete.transittracker.models.place.ParcelablePlace
import com.crepetete.transittracker.models.place.PlacesController
import com.gjiazhe.scrollparallaximageview.ScrollParallaxImageView
import com.gjiazhe.scrollparallaximageview.parallaxstyle.VerticalMovingStyle
import com.google.android.gms.location.places.GeoDataClient
import timber.log.Timber


class PlaceViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    private var mPlaceId: String = ""

    private val mTitleTextView by lazy { view.findViewById<TextView>(R.id.title) }
    private val mDescTextView by lazy { view.findViewById<TextView>(R.id.description) }
    private val mButtonRemove by lazy { view.findViewById<Button>(R.id.button_remove) }
    private val mImageBanner: ImageView = view.findViewById(R.id.banner)

    private var mImage: Bitmap? = null

    fun setPlace(place: ParcelablePlace) {
        mPlaceId = place.id

        mTitleTextView.text = place.name
        mDescTextView.text = place.address
        mButtonRemove.setOnClickListener({
            PlacesController.removePlace(place.id)
        })
    }

    private fun setImageVisibility(visibility: Int) {
        mImageBanner.visibility = visibility
    }

    fun setImage(geoDataClient: GeoDataClient?) {
        setImageVisibility(View.GONE)
        if (geoDataClient == null || mPlaceId.isBlank()) {
            return
        }
//        if (mImageBanner is ScrollParallaxImageView) {
//            mImageBanner.setParallaxStyles(MyParallaxStyle())
//        }

        val v = view.findViewById<ViewGroup>(R.id.card_content)
        v.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        if (mImage != null) {
            mImageBanner.setImageBitmap(mImage)
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
                                updateImageBanner(photoResponseTask.result.bitmap)
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

    private fun updateImageBanner(bitmap: Bitmap) {
        mImage = bitmap
        mImageBanner.setImageBitmap(bitmap)
        mImageBanner.visibility = View.VISIBLE
    }

    private inner class MyParallaxStyle : VerticalMovingStyle() {
        override fun transform(view: ScrollParallaxImageView, canvas: Canvas, x: Int, y: Int) {
            if (view.scaleType != ImageView.ScaleType.CENTER_CROP) {
                return
            }

            // image's width and height
            val iWidth = view.drawable.intrinsicWidth
            val iHeight = view.drawable.intrinsicHeight
            if (iWidth <= 0 || iHeight <= 0) {
                return
            }

            // view's width and height
            val vWidth = view.width - view.paddingLeft - view.paddingRight
            val vHeight = view.height - view.paddingTop - view.paddingBottom

            // device's height
            val dHeight = view.resources.displayMetrics.heightPixels

            var mY = y

            if (iWidth * vHeight < iHeight * vWidth) {
                // avoid over scroll
                if (mY < -vHeight) {
                    mY = -vHeight
                } else if (mY > dHeight) {
                    mY = dHeight
                }

                val imgScale = vWidth.toFloat() / iWidth.toFloat()
                val max_dy = Math.abs((iHeight * imgScale - vHeight) * 0.5f)
                val translateY = -(2f * max_dy * mY.toFloat() + max_dy * (vHeight - dHeight)) / (vHeight + dHeight)
                canvas.translate(0f, translateY)
            }
        }
    }
}