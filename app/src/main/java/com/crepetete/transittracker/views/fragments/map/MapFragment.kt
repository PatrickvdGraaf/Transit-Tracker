package com.crepetete.transittracker.views.fragments.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.annotation.CallSuper
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.view.animation.BounceInterpolator
import com.crepetete.transittracker.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import timber.log.Timber

/**
 *
 * Created by Patrick van de Graaf on 5/28/2018.
 *
 */
abstract class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener {
    private var mLocationRequest: LocationRequest? = null

    private val mRequestPermissionsRequestCode = 34

    private var mLastLocation: Location? = null

    private lateinit var mMap: GoogleMap

    private val mFusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context!!)
    }

    private val mMarkers = arrayListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getLastKnownLocation()
    }

    /**
     * Return the current state of the permissions needed.
     */
    protected fun checkPermissions(): Boolean {
        val permissionState = context
                ?.let { context ->
                    ActivityCompat.checkSelfPermission(context,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                } ?: PackageManager.PERMISSION_DENIED
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Required method of the [OnMapReadyCallback] interface.
     * Called when the [GoogleMap] was loaded or produced an error (null).
     */
    @CallSuper
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.let { map ->
            mMap = map
            mMap.setOnMapClickListener(this)
            mMap.setOnMarkerClickListener(this)
            if (checkPermissions()) {
                mMap.isMyLocationEnabled = true
            }
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
                    onPermissionsGranted()
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
                    onPermissionsDenied()
                }
            }
        }
    }

    abstract fun onPermissionsGranted()

    abstract fun onPermissionsDenied()

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        if (checkPermissions()) {
            mFusedLocationClient.lastLocation?.addOnSuccessListener { location ->
                mLastLocation = location
            }
            startLocationUpdates()
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, LocationCallback(), null)
    }

    protected fun addMarker(options: MarkerOptions) {
        mMap.addMarker(options)?.let { marker ->
            if (mMarkers.none { savedMarker ->
                        marker.position == savedMarker.position
                    }) {
                dropPinEffect(marker)
                mMarkers.add(marker)
            }
        }
    }

    protected fun drawCircle(options: CircleOptions, onComplete: (circle: Circle) -> Unit) {
        mMap.addCircle(options)?.let { circle -> onComplete(circle) }
    }

    /**
     * Animates the Map to a position where all Geofences and the user's location is visible.
     */
    protected fun centerOnAllMarkers() {
        val builder = LatLngBounds.builder()
        mMarkers.map { builder.include(it.position) }
        mLastLocation?.let { location ->
            builder.include(LatLng(location.latitude, location.longitude))
        }

        val cameraUpdate = CameraUpdateFactory
                .newLatLngBounds(builder.build(), context?.resources
                        ?.getDimensionPixelSize(R.dimen.margin_huge) ?: 64)

        mMap.animateCamera(cameraUpdate, 2500, null)
    }

    private fun dropPinEffect(marker: Marker) {
        val handler = Handler()
        val start = SystemClock.uptimeMillis()
        val duration: Long = 2000
        val interp = BounceInterpolator()
        handler.post(object : Runnable {
            override fun run() {
                val elapsed = SystemClock.uptimeMillis() - start
                val t = Math.max((1 - interp.getInterpolation((elapsed.toFloat() / duration))), 0F)
                marker.setAnchor(0.5f, 1.0f + 14 * t)
                if (t > 0.0) {
                    // Post again 15ms later.
                    handler.postDelayed(this, 15)
                } else {
                    marker.showInfoWindow()
                }
            }
        })
    }

    protected fun requestPermissions() {
        activity?.let { activity ->
            with(activity as Activity) {
                val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                // Provide an additional rationale to the user. This would happen if the user denied the
                // request previously, but didn't check the "Don't ask again" checkbox.
                if (shouldProvideRationale) {
                    Timber.i("Displaying  permission rationale to provide additional context.")
//                    Snackbar.make(mMap, "", Snackbar.LENGTH_SHORT)
//                            .setAction(android.R.string.ok, {
//                                // Request permission
//                                ActivityCompat.requestPermissions(activity,
//                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                                        mRequestPermissionsRequestCode)
//                            })
                } else {
                    Timber.i("Requesting permission")

                    // Request permission. It's possible this can be auto answered if device policy
                    // sets the permission in a given state or the user denied the permission
                    // previously and checked "Never ask again".
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            mRequestPermissionsRequestCode)

                }
            }
        }

    }

}