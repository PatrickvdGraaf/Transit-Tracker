package com.crepetete.transittracker.models.place

import android.content.Context
import android.preference.PreferenceManager
import com.crepetete.transittracker.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.places.GeoDataClient

object PlacesController {
    private const val GEOFENCE_EXPIRATION_IN_HOURS = 1L
    private const val GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000

    private val mListeners = hashMapOf<String, PlacesListener>()
    private val mPlaces: MutableList<ParcelablePlace> = mutableListOf()

    fun getGeofenceObjects(context: Context): List<Geofence> {
        val geofences = arrayListOf<Geofence>()
        for (place in mPlaces) {
            geofences.add(fromParcelablePlace(context, place))
        }
        return geofences
    }

    private fun fromParcelablePlace(context: Context, place: ParcelablePlace): Geofence {
        return Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(place.id)

                // Set the circular region of this geofence.
                // Recommended value is a 100 meters for most situations. If the geofence is in the
                // countryside, 500 meters could be used.

                .setCircularRegion(place.latLng.latitude,
                        place.latLng.longitude,
                        getGeofenceRadiusFromPrefs(context)
                )
                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                // Set to 12 hours for now, but this value could be decreased since most travels
                // don't take that long.
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
    }

    fun getGeofenceRadiusFromPrefs(context: Context): Float {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val radius = prefs.getString(context.getString(R.string.pref_geofence_radius),
                "500").toFloat()
        return radius
    }

    fun getImageForPlace(id: String, geoDataClient: GeoDataClient) {
        val imagePosition = getPositionForId(id)
        if (imagePosition != -1) {
            geoDataClient.getPlacePhotos(id).addOnCompleteListener { task ->
                // Get the PlacePhotoMetadataBuffer (metadata for all the photos).
                val photoMetadataBuffer = task.result.photoMetadata
                if (photoMetadataBuffer.count > 0) {
                    // Get first photo in the list.
                    val photoMetadata = photoMetadataBuffer[0]
                    // Get the attribution text.
                    // TODO show attribution
                    val attribution = photoMetadata.attributions
                    // Get a full-size bitmap for the photo
                    geoDataClient.getPhoto(photoMetadata).addOnCompleteListener { photoTask ->
                        if (photoTask.isSuccessful) {
                            val place = getPlaceForId(id)
                            if (place != null) {
                                place.setImage(photoTask.result.bitmap)
                                notifyListeners(imagePosition)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun notifyListeners(position: Int) {
        for (listener in mListeners.values) {
            listener.onPlacesChanged(position)
        }
    }

    private fun notifyRemoval(position: Int) {
        for (listener in mListeners.values) {
            listener.onPlacesChanged(position)
        }
    }

    fun addPlace(place: ParcelablePlace) {
        mPlaces.add(place)
        notifyListeners(getNumberOfPlaces())
    }

    fun addListener(listener: PlacesListener) {
        mListeners[listener.getListenerTag()] = listener
    }

    fun removePlace(id: String) {
        val position = getPositionForId(id)
        mPlaces.removeAt(position)
        notifyRemoval(position)
    }

    fun removeListener(tag: String) {
        mListeners.remove(tag)
    }

    fun getPlaces(): List<ParcelablePlace> {
        return mPlaces
    }

    private fun getPlaceForId(id: String): ParcelablePlace? {
        for (place in mPlaces) {
            if (place.id == id) {
                return place
            }
        }
        return null
    }

    private fun getPositionForId(id: String): Int {
        for ((i, place) in mPlaces.withIndex()) {
            if (place.id == id) {
                return i
            }
        }
        return -1
    }

    fun getNumberOfPlaces(): Int {
        return mPlaces.size
    }
}