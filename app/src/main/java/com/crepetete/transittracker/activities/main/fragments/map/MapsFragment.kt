package com.crepetete.transittracker.activities.main.fragments.map


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.crepetete.transittracker.BuildConfig
import com.crepetete.transittracker.R
import com.crepetete.transittracker.config.Constants
import com.crepetete.transittracker.config.GeofenceErrorMessages
import com.crepetete.transittracker.intent.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.fragment_maps.*
import timber.log.Timber

class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapClickListener,
        OnCompleteListener<Void> {
    private val mRequestPermissionsRequestCode = 34

    /**
     * Tracks whether the user requested to add or remove geofences, or to do neither.
     */
    private enum class PendingGeofenceTask {
        ADD, REMOVE, NONE
    }

    private lateinit var mMap: GoogleMap
    /**
     * Provides access to the Geofencing API
     */
    private var mGeofencingClient: GeofencingClient? = null

    /**
     * List of geofences on the Map
     */
    private var mGeofenceList = arrayListOf<Geofence>()

    /**
     * Used when requesting to add or remove geofences
     */
    private var mGeofencePendingIntent: PendingIntent? = null

    private var mPendingGeofenceTask = PendingGeofenceTask.NONE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_maps, container, false)
//        val mapFragment = childFragmentManager.findFragmentById(R.id.main_branch_map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
        return rootView
    }

    override fun onStart() {
        super.onStart()
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            performPendingGeofenceTask()
        }
    }

    /**
     * Required method of the {@link OnMapReadyCallback} interface
     */
    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            mMap = googleMap
            mGeofencingClient = context?.let { LocationServices.getGeofencingClient(it) }
            mMap.setOnMapClickListener(this)

            // Get the geofences used. Geofence data is hard coded in this sample.
            populateGeofenceList()
        } else {
            Snackbar.make(content, "Error: could not load map", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Retry".toUpperCase(), {
//                        val mapFragment =
//                                childFragmentManager.findFragmentById(R.id.main_branch_map)
//                                        as SupportMapFragment
//                        mapFragment.getMapAsync(this)
                    }).show()
        }
    }

    /**
     * Required method of the {@link GoogleMap.OnMapClickListener} interface
     */
    override fun onMapClick(position: LatLng?) {
        if (position != null) {
            mMap.addMarker(MarkerOptions().position(position).title("TITLE")).showInfoWindow()
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 17.0f))
        }
    }

    /**
     * Required method of the {@link OnCompleteListener} interface
     * Runs when the result of calling {@link #addGeofences()} and/or {@link #removeGeofences()}
     * is available.
     *
     * @param task the resulting Task, containing either a result or error.
     */
    override fun onComplete(task: Task<Void>) {
        mPendingGeofenceTask = PendingGeofenceTask.NONE
        if (task.isSuccessful) {
            updateGeofencesAdded(!getGeofenceAdded())
            Toast.makeText(context,
                    if (getGeofenceAdded())
                        "Geofence successfully added"
                    else
                        "Geofence successfully removed",
                    Toast.LENGTH_SHORT).show()
        } else {
            Timber.w(context?.let {
                GeofenceErrorMessages.getErrorString(it, task.exception
                        ?: Exception())
            })
        }
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private fun getGeofencingRequest(): GeofencingRequest {
        val builder = GeofencingRequest.Builder()

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

        // Add the geofences to be monitored by geofencing service
        builder.addGeofences(mGeofenceList)

        return builder.build()
    }

    fun addGeofenceButtonPressed(key: String, position: LatLng) {
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.ADD
            requestPermissions()
            return
        }
//        addGeoFence(key, position)
    }

    @SuppressLint("MissingPermission")
    private fun addGeoFences() {
        if (!checkPermissions()) {
            Snackbar.make(content, R.string.insufficient_permissions, Snackbar.LENGTH_LONG)
                    .setAction("Edit".toUpperCase(), {
                        requestPermissions()
                    }).show()
            return
        }
        if (mGeofencingClient != null) {
            mGeofencingClient!!.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnCompleteListener(this)
        }
    }

    fun removeGeofenceButtonPressed(key: String) {
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.REMOVE
            requestPermissions()
            return
        }
        removeGeofence()
    }

    private fun removeGeofence() {
        if (!checkPermissions()) {
            Snackbar.make(content, "Missing permissions", Snackbar.LENGTH_LONG)
                    .setAction("Edit".toUpperCase(), {
                        requestPermissions()
                    }).show()
            return
        }

        if (mGeofencingClient != null) {
            mGeofencingClient!!.removeGeofences(getGeofencePendingIntent())
                    .addOnCompleteListener(this)
        }
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private fun getGeofencePendingIntent(): PendingIntent? {
        // Reuse the PendingIntent is we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent
        }
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        return mGeofencePendingIntent
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     *
     * TODO do just that.
     */
    private fun populateGeofenceList() {
        for (entry in Constants.BAY_AREA_LANDMARKS) {
            mGeofenceList.add(Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.key)

                    // Set the circular region of this geofence.
                    .setCircularRegion(entry.value.latitude,
                            entry.value.longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )
                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build()
            )
        }
    }

    /**
     * Returns true if geofences were added, otherwise false.
     */
    private fun getGeofenceAdded(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Constants.GEOFENCES_ADDED_KEY, false)
    }

    /**
     * Stores whether geofences were added ore removed in {@link SharedPreferences};
     *
     * @param added Whether geofences were added or removed.
     */
    private fun updateGeofencesAdded(added: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(Constants.GEOFENCES_ADDED_KEY, added)
                .apply()
    }

    /**
     * Performs the geofencing task that was pending until location permission was granted.
     * TODO change to real value
     */
    private fun performPendingGeofenceTask() {
        if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
            addGeoFences()
        } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
            removeGeofence()
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
            Snackbar.make(content, R.string.permission_rationale, Snackbar.LENGTH_SHORT)
                    .setAction(android.R.string.ok, {
                        // Request permission
                        ActivityCompat.requestPermissions(activity as Activity,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                mRequestPermissionsRequestCode)
                    })
        } else {
            Timber.i("Requesting permission")

            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(activity as Activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    mRequestPermissionsRequestCode)

        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        Timber.i("onRequestPermissionResult")
        if (requestCode == mRequestPermissionsRequestCode) {
            when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request is cancelled and
                    // you receive empty arrays.
                    Timber.i("User interaction was cancelled")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    Timber.i("Permission granted")
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
                    Snackbar.make(content, R.string.permission_denied_explanation,
                            Snackbar.LENGTH_LONG)
                            .setAction(R.string.settings, {
                                // Build intent that displays the App settings screen.
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                val uri = Uri.fromParts(
                                        "package",
                                        BuildConfig.APPLICATION_ID,
                                        null)
                                intent.data = uri
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            })
                    mPendingGeofenceTask = PendingGeofenceTask.NONE
                }
            }
        }
    }
}
