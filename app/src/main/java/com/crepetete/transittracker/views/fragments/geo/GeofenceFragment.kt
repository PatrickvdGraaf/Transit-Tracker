package com.crepetete.transittracker.views.fragments.geo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.startForegroundService
import android.support.v4.graphics.ColorUtils
import android.widget.Toast
import com.crepetete.transittracker.R
import com.crepetete.transittracker.config.Constants
import com.crepetete.transittracker.models.intent.broadcast.GeofenceBroadCastReceiver
import com.crepetete.transittracker.models.intent.service.GeofenceService
import com.crepetete.transittracker.models.place.PlaceData
import com.crepetete.transittracker.models.place.PlacesController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.MarkerOptions
import timber.log.Timber

abstract class GeofenceFragment : MapFragment(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    companion object {
        private const val NONE = 0
        private const val ADD = 1
        private const val REMOVE = 2
    }

    private var mPendingGeofenceTask = NONE

    /**
     * Provides the entry point to Geofence services.
     */
    private val mGeofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(context!!)
    }

    /**
     * Used to keep track of whether geofences were added.
     */
    private var mGeofencesAdded = false

    /**
     * Used when requesting to add or remove geofences.
     */
    private var mGeofencePendingIntent: PendingIntent? = null

    /**
     * Used to persist application state about whether geofences were added.
     */
    private var mSharedPreferences: SharedPreferences? = null

    private val mCircles = arrayListOf<Circle>()

    private val mGoogleApiClient: GoogleApiClient by lazy {
        GoogleApiClient.Builder(context!!)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Intent(context, GeofenceService::class.java).let { service ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(context!!, service)
            } else {
                activity!!.startService(service)
            }
        }

        val manager = context?.getSystemService(Context.LOCATION_SERVICE)
                as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null
        // Retrieve an instance of the SharedPreferences object.
        mSharedPreferences = context?.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE)

        // Get the value of mGeofencesAdded from SharedPreferences. Set to false as a default.
        mGeofencesAdded = mSharedPreferences?.getBoolean(Constants.GEOFENCES_ADDED_KEY,
                false)!!

        // Kick off the request to getBuilder GoogleApiClient.
        createGoogleApi()
    }

    private fun createGoogleApi() {
        mPendingGeofenceTask = ADD
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            if (!mGoogleApiClient.isConnected) {
                mGoogleApiClient.connect()
            }
        }
    }

    private fun buildAlertMessageNoGps() {
        AlertDialog.Builder(context)
                .setCancelable(false)
                .setPositiveButton(getString(android.R.string.yes)) { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton(getString(android.R.string.no)) { dialog, _ ->
                    dialog.cancel()
                }
                .create().show()
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient.connect()
    }

    /**
     * Disconnects Google's API client.
     */
    override fun onStop() {
        super.onStop()
        mGoogleApiClient.disconnect()
    }

    /**
     * Required method of the [OnMapReadyCallback] interface.
     * Called when the [GoogleMap] was loaded or produced an error (null).
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        super.onMapReady(googleMap)
        googleMap?.let { drawGeofences() }
    }

    override fun onPermissionsDenied() {
        performPendingGeofenceTask()
    }

    override fun onPermissionsGranted() {
        mPendingGeofenceTask = NONE
    }

    /**
     * Performs the geofencing task that was pending until location permission was granted.
     */
    @SuppressLint("MissingPermission")
    private fun performPendingGeofenceTask() {
        if (mPendingGeofenceTask == ADD) {
            try {
                if (context == null) {
                    Timber.d("Couldn't start geofences, context was null.")
                    return
                }

                val builder = with(GeofencingRequest.Builder()) {
                    setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    // Add the geofences to be monitored by geofencing service.
                    addGeofences(PlacesController.getGeofenceObjects(context!!))
                }

                mGeofencingClient.addGeofences(builder.build(), getGeofencePendingIntent())
                        ?.addOnSuccessListener {
                            Toast.makeText(context!!, "Geofences successfully added.",
                                    Toast.LENGTH_SHORT).show()
                            mPendingGeofenceTask = NONE

                            // Update state and save in shared preferences.
                            mSharedPreferences?.edit()
                                    ?.putBoolean(Constants.GEOFENCES_ADDED_KEY, mGeofencesAdded)
                                    ?.apply()
                        }
                        ?.addOnFailureListener {
                            Toast.makeText(context!!,
                                    "Failed to add Geofences",
                                    Toast.LENGTH_SHORT).show()

                            mGoogleApiClient.disconnect()
                            activity!!.stopService(Intent(context, GeofenceService::class.java))
                            Timber.e(it)
                        }
            } catch (ex: Throwable) {
                Timber.e("addUserDefinedGeoFence Exception: $ex.message")
            }
        } else if (mPendingGeofenceTask == REMOVE) {
            removeGeofence()
        }
    }

    private fun removeGeofence() {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener {
                    Toast.makeText(context!!, "Geofences successfully removed.",
                            Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context!!,
                            "Failed to remove geofences.",
                            Toast.LENGTH_SHORT).show()
                }
    }

    protected fun drawGeofences() {
        mCircles.forEach { it.remove() }

        val strokeColor = ColorUtils.setAlphaComponent(ContextCompat.getColor(context!!,
                R.color.colorPrimaryDark),
                100)
        val fillColor = ColorUtils.setAlphaComponent(ContextCompat.getColor(context!!,
                R.color.colorPrimaryLight),
                100)

        PlacesController.getPlaces().map { place ->
            markerForGeofence(place)

            with(CircleOptions()) {
                center(place.getLatLng())
                strokeColor(strokeColor)
                strokeWidth(1F)
                fillColor(fillColor)
                radius(PlacesController.getGeofenceRadiusFromPrefs(context).toDouble())

                drawCircle(this, { circle -> mCircles.add(circle) })
            }
        }

        centerOnAllMarkers()
    }

    private fun markerForGeofence(parcelablePlace: PlaceData) {
        val hsv = FloatArray(3)
        Color.colorToHSV(ContextCompat.getColor(context!!, R.color.colorPrimary), hsv)
        val markerOptions = MarkerOptions()
                .position(parcelablePlace.getLatLng())
                .icon(BitmapDescriptorFactory.defaultMarker(hsv[0]))
                .title(parcelablePlace.name)
                .snippet(parcelablePlace.address)
        addMarker(markerOptions)

    }

    private fun getGeofencePendingIntent(): PendingIntent {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent!!
        }
        val intent = Intent(GeofenceBroadCastReceiver.GEOFENCE_ACTION)
        return PendingIntent.getBroadcast(context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    override fun onConnected(p0: Bundle?) {
        Timber.i("Connected to GoogleApiClient")
        if (checkPermissions()) {
            performPendingGeofenceTask()
        } else {
            requestPermissions()
        }
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Timber.e("Connection failed: ConnectionResult.getErrorCode() = ${result.errorCode}")
    }

    /**
     * Method from the [GoogleApiClient.ConnectionCallbacks] interface.
     */
    override fun onConnectionSuspended(p0: Int) {
        Timber.w("onConnectionSuspended()")
    }
}