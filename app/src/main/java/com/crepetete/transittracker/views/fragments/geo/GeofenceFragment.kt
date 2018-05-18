package com.crepetete.transittracker.views.fragments.geo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.startForegroundService
import android.support.v4.graphics.ColorUtils
import android.view.animation.BounceInterpolator
import android.widget.Toast
import com.crepetete.transittracker.R
import com.crepetete.transittracker.config.Constants
import com.crepetete.transittracker.models.intent.broadcast.GeofenceBroadCastReceiver
import com.crepetete.transittracker.models.intent.service.GeofenceService
import com.crepetete.transittracker.models.place.ParcelablePlace
import com.crepetete.transittracker.models.place.PlacesController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import timber.log.Timber

abstract class GeofenceFragment : Fragment(), GoogleApiClient.ConnectionCallbacks,
        OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

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

    private var mLocationRequest: LocationRequest? = null

    private val mCircles: ArrayList<Circle> = arrayListOf()

    private val mRequestPermissionsRequestCode = 34

    private var mLastLocation: Location? = null

    private val mHashMap: HashMap<Marker, ParcelablePlace> = hashMapOf()

    private val mGoogleApiClient: GoogleApiClient by lazy {
        GoogleApiClient.Builder(context!!)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }

    private val mFusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context!!)
    }

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val service = Intent(context, GeofenceService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(context!!, service)
        } else {
            activity!!.startService(service)
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

        getLastKnownLocation()
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
        val builder = AlertDialog.Builder(context)
                .setCancelable(false)
                .setPositiveButton(getString(android.R.string.yes)) { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton(getString(android.R.string.no)) { dialog, _ ->
                    dialog.cancel()
                }
        val alert = builder.create()
        alert.show()
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
        if (googleMap != null) {
            mMap = googleMap
            mMap.setOnMapClickListener(this)
            mMap.setOnMarkerClickListener(this)
            if (checkPermissions()) {
                mMap.isMyLocationEnabled = true
            }
            drawGeofences()
        } else {
//            Snackbar.make(content, "Error: could not load map", Snackbar.LENGTH_INDEFINITE)
//                    .setAction("Retry".toUpperCase(), {
//                        createMap()
//                    }).show()
        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions(): Boolean {
        val permissionState = context?.let {
            ActivityCompat.checkSelfPermission(it,
                    Manifest.permission.ACCESS_FINE_LOCATION)
        } ?: PackageManager.PERMISSION_DENIED
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity as Activity,
                Manifest.permission.ACCESS_FINE_LOCATION)

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Timber.i("Displaying  permission rationale to provide additional context.")
//            Snackbar.make(mMap, R.string.permission_rationale,
//                    Snackbar.LENGTH_SHORT).setAction(android.R.string.ok, {
//                // Request permission
//                ActivityCompat.requestPermissions(activity as Activity,
//                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                        mRequestPermissionsRequestCode)
//            })
        } else {
            Timber.i("Requesting permission")

            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    mRequestPermissionsRequestCode)

        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == mRequestPermissionsRequestCode) {
            when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request is cancelled and
                    // you receive empty arrays.
                    Timber.i("User interaction was cancelled")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    Timber.i("Permission granted")
                    getLastKnownLocation()
                    performPendingGeofenceTask()
                }
                else -> {
                    // Permission denied.

                    // Notify the user via a SnackBar that they have rejected a core permission for
                    // the app, which makes the Activity useless. In a real app, core permissions
                    // would typically be best requested during a welcome-screen flow.

                    // Additionally, it is important to remember that a permission might have been
                    // rejected without asking the user for permission (device policy or "Never ask
                    // again" prompts). Therefore, a user interface affordance is typically
                    // implemented when permissions are denied. Otherwise, your app could appear
                    // unresponsive to touches or interactions which have required permissions.
//                    Snackbar.make(content, R.string.permission_denied_explanation,
//                            Snackbar.LENGTH_LONG)
//                            .setAction(R.string.settings, {
//                                // Build intent that displays the App settings screen.
//                                val intent = Intent()
//                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                                val uri = Uri.fromParts(
//                                        "package",
//                                        BuildConfig.APPLICATION_ID,
//                                        null)
//                                intent.data = uri
//                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                                startActivity(intent)
//                            })
                    mPendingGeofenceTask = NONE
                }
            }
        }
    }

    /**
     * Performs the geofencing task that was pending until location permission was granted.
     */
    @SuppressLint("MissingPermission")
    private fun performPendingGeofenceTask() {
        if (mPendingGeofenceTask == ADD) {
//            removeGeofence()

            try {
                if (context == null) {
                    Timber.d("Couldn't start geofences, context was null.")
                    return
                }

                val builder = GeofencingRequest.Builder()
                builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                // Add the geofences to be monitored by geofencing service.
                builder.addGeofences(PlacesController.getGeofenceObjects(context!!))

                mGeofencingClient.addGeofences(builder.build(), getGeofencePendingIntent())
                        ?.addOnSuccessListener {
                            Toast.makeText(context!!, "Geofences successfully added.",
                                    Toast.LENGTH_SHORT).show()
                            mPendingGeofenceTask = NONE

                            // Update state and save in shared preferences.
                            val editor = mSharedPreferences?.edit()
                            editor?.putBoolean(Constants.GEOFENCES_ADDED_KEY, mGeofencesAdded)
                            editor?.apply()
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

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        if (checkPermissions()) {
            context?.let {
                mFusedLocationClient.lastLocation?.addOnSuccessListener {
                    mLastLocation = it
                }
            }
            startLocationUpdates()
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        if (checkPermissions()) {
            context?.let {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, LocationCallback(), null)
            }
        }
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


    private fun removeGeofence() {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent())?.addOnSuccessListener {
            Toast.makeText(context!!, "Geofences successfully removed.",
                    Toast.LENGTH_SHORT).show()
        }
                ?.addOnFailureListener {
                    Toast.makeText(context!!,
                            "Failed to remove geofences.",
                            Toast.LENGTH_SHORT).show()
                }
    }

    protected fun drawGeofences() {
        if (!mCircles.isEmpty()) {
            for (circle in mCircles) {
                circle.remove()
            }
        }

        val places = PlacesController.getPlaces()
        for (place in places) {
            markerForGeofence(place)
            val circleOptions = CircleOptions()
                    .center(place.latLng)
                    .strokeColor(ColorUtils.setAlphaComponent(ContextCompat.getColor(context!!,
                            com.crepetete.transittracker.R.color.colorPrimaryDark),
                            100))
                    .strokeWidth(1F)
                    .fillColor(ColorUtils.setAlphaComponent(ContextCompat.getColor(context!!,
                            R.color.colorPrimaryLight),
                            100))
                    .radius(PlacesController.getGeofenceRadiusFromPrefs(context!!).toDouble())
            mMap.addCircle(circleOptions)?.let { mCircles.add(it) }
        }

//        centerOnGeofences()
    }

    private fun markerForGeofence(parcelablePlace: ParcelablePlace) {
        val hsv = FloatArray(3)
        Color.colorToHSV(ContextCompat.getColor(context!!, R.color.colorPrimaryDark), hsv)
        val markerOptions = MarkerOptions()
                .position(parcelablePlace.latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(hsv[0]))
                .title(parcelablePlace.name)
                .snippet(parcelablePlace.address)
        val marker = mMap.addMarker(markerOptions)

        if (marker != null) {
            dropPinEffect(marker)
            mHashMap[marker] = parcelablePlace
        }
    }


    private fun dropPinEffect(marker: Marker) {
        val handler = Handler()
        val start = SystemClock.uptimeMillis()
        val duration: Long = 2000
        val interpolator = BounceInterpolator()
        handler.post(object : Runnable {
            override fun run() {
                val elapsed = SystemClock.uptimeMillis() - start
                val t = Math.max(
                        (1 - interpolator.getInterpolation((elapsed.toFloat() / duration))), 0F)
                marker.setAnchor(0.5f, 1.0f + 14 * t)
                if (t > 0.0) {
                    // Post again 15ms later.
                    handler.postDelayed(this, 15)
                } else {
                    marker.showInfoWindow()
                    centerOnGeofences()
                }
            }
        })
    }

    /**
     * Animates the Map to a position where all Geofences and the user's location is visible.
     */
    private fun centerOnGeofences() {
        if (mHashMap.keys.size > 0) {
            val builder = LatLngBounds.builder()
            for (marker in mHashMap.keys) {
                builder.include(marker.position)
            }

            if (mLastLocation != null) {
                builder.include(LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude))
            }

            val cameraUpdate = CameraUpdateFactory
                    .newLatLngBounds(builder.build(), context?.resources
                            ?.getDimensionPixelSize(R.dimen.margin_huge) ?: 64)
            mMap.animateCamera(cameraUpdate, 4000, null)
        }
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