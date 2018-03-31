package com.crepetete.transittracker.activities.main.fragments


import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.crepetete.transittracker.R
import com.crepetete.transittracker.intent.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
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


class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapClickListener,
        OnCompleteListener<Void> {
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
    private var mGeofenceList = emptyArray<Geofence>()

    /**
     * Used when requesting to add or remove geofences
     */
    private var mGeofencePendingIntent: PendingIntent? = null

    private var mPendingGeofenceTask = PendingGeofenceTask.NONE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_maps, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.main_branch_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
        } else {
            Snackbar.make(content, "Error: could not load map", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Retry".toUpperCase(), {
                        val mapFragment =
                                childFragmentManager.findFragmentById(R.id.main_branch_map)
                                        as SupportMapFragment
                        mapFragment.getMapAsync(this)
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
            updateGeofencesAdded(!geofencesAdded())
            Toast.makeText(context, getGeofenceAdded() ? "Geofence successfully added"
            : "Geofence successfully removed", Toast.LENGHT_SHORT).show()
        } else {
            Log.w("MAPS FRAGMENT", GeofenceErrorMessages.getErrorString(this, task.exception))
        }
    }

    fun addGeofenceButtonPressed(key: String, position: LatLng) {
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.ADD
            requestPermissions()
            return
        }
        addGeoFence(key, position)
    }

    @SuppressLint("MissingPermission")
    private fun addGeoFence(key: String, position: LatLng?) {
        if (!checkPermissions()) {
            Snackbar.make(content, "Missing permissions", Snackbar.LENGTH_LONG)
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
        removeGeofence(key)
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

    private fun getGeofencePendingIntent: PendingIntent {
        // Reuse the PendingIntent is we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent
        }
        val intent = Intent(context, GeofenceBroadcastReceiver.class)
    }
}
