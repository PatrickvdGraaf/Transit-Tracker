package com.crepetete.transittracker.models.place

import com.google.android.gms.location.Geofence

object PlacesController {
    private const val GEOFENCE_EXPIRATION_IN_HOURS = 1L
    private const val GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000
    const val GEOFENCE_RADIUS_IN_METERS = 100.0f

    private val mListeners = hashMapOf<String, PlacesListener>()
    private val mPlaces: MutableList<ParcelablePlace> = mutableListOf()

    fun getGeofenceObjectsForPlaces(): Map<String, Geofence> {
        val geofences = hashMapOf<String, Geofence>()
        for (place in mPlaces) {
            geofences[place.id] = fromParcelablePlace(place)
        }
        return geofences
    }

    fun getGeofenceObjects(): List<Geofence> {
        val geofences = arrayListOf<Geofence>()
        for (place in mPlaces) {
            geofences.add(fromParcelablePlace(place))
        }
        return geofences
    }

    private fun fromParcelablePlace(place: ParcelablePlace): Geofence {
        return Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(place.id)

                // Set the circular region of this geofence.
                // Recommended value is a 100 meters for most situations. If the geofence is in the
                // countryside, 500 meters could be used.
                .setCircularRegion(place.latLng.latitude,
                        place.latLng.longitude,
                        GEOFENCE_RADIUS_IN_METERS
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

    private fun notifyListeners(position: Int) {
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
        val place = getPlaceForId(id)
        val position = mPlaces.indexOf(place)

        mPlaces.remove(place)
        notifyListeners(position)
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

    fun getNumberOfPlaces(): Int {
        return mPlaces.size
    }
}