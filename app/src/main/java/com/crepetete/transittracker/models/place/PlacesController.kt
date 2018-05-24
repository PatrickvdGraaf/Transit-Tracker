package com.crepetete.transittracker.models.place

import android.content.Context
import android.preference.PreferenceManager
import android.widget.Toast
import com.crepetete.transittracker.R
import com.crepetete.transittracker.models.database.DatabaseWorkerThread
import com.crepetete.transittracker.models.database.PlaceDatabase
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.places.GeoDataClient
import timber.log.Timber

object PlacesController {
    private const val GEOFENCE_EXPIRATION_IN_HOURS = 1L
    private const val GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000

    private val mListeners = hashMapOf<String, PlacesListener>()
    private val mPlaces: MutableList<PlaceData> = mutableListOf()

    private var mDatabase: PlaceDatabase? = null

    private var mDatabaseWorkerThread = DatabaseWorkerThread()

    // Local
    fun addPlace(place: PlaceData) {
        mPlaces.add(place)
        notifyListeners(getNumberOfPlaces())
    }

    fun removePlace(id: String) {
        val position = getPositionForId(id)
        mPlaces.removeAt(position)
        notifyRemoval(position)
    }

    // Database
    fun insertPlace(context: Context, place: PlaceData) {
        mDatabaseWorkerThread.postTask(Runnable {
            mDatabase?.placeDataDao()?.insert(place)
            Toast.makeText(context, "Successfully saved ${place.name}", Toast.LENGTH_SHORT)
                    .show()
        })
    }

    fun deletePlace(context: Context, place: PlaceData) {
        mDatabaseWorkerThread.postTask(Runnable {
            mDatabase?.placeDataDao()?.delete(place)
            Toast.makeText(context, "Successfully deleted ${place.name}", Toast.LENGTH_SHORT)
                    .show()
        })
    }

    fun getAllFromDatabase(onComplete: (List<PlaceData>) -> Unit = {
        Timber.d("No onComplete set for getAllFromDatabase()")
    }) {
        mDatabaseWorkerThread.postTask(Runnable {
            mDatabase?.placeDataDao()?.getAll()?.let { onComplete(it) }
        })
    }

    /**
     * Creates a List of Geofences from all Places in mPlaces
     */
    fun getGeofenceObjects(context: Context): List<Geofence> {
        return mPlaces.map { fromParcelablePlace(context, it) }
    }

    /**
     * Build a Geofence object for a specific place
     */
    private fun fromParcelablePlace(context: Context, place: PlaceData): Geofence {
        return Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(place.id)

                // Set the circular region of this geofence.
                // Recommended value is a 100 meters for most situations. If the geofence is in the
                // countryside, 500 meters could be used.
                .setCircularRegion(place.latitude,
                        place.longitude,
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
        return prefs.getString(context.getString(R.string.pref_geofence_radius),
                "500").toFloat()
    }

    fun loadImageForPlace(id: String, geoDataClient: GeoDataClient) {
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
                                place.setBitmap(photoTask.result.bitmap)
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

    fun addListener(listener: PlacesListener) {
        mListeners[listener.getListenerTag()] = listener
    }

    fun removeListener(tag: String) {
        mListeners.remove(tag)
    }

    fun getPlaces(): List<PlaceData> {
        return mPlaces
    }

    private fun getPlaceForId(id: String): PlaceData? {
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

    fun isEmpty(): Boolean {
        return mPlaces.isEmpty()
    }

    fun onStart(context: Context) {
        mDatabase = PlaceDatabase.getInstance(context)
        mDatabaseWorkerThread.start()
    }

    fun onStop() {
        mDatabaseWorkerThread.quit()
        PlaceDatabase.destroyInstance()
    }
}