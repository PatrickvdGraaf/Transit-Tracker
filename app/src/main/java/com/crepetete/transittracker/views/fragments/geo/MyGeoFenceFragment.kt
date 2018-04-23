package com.crepetete.transittracker.views.fragments.geo


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.crepetete.transittracker.R
import com.crepetete.transittracker.models.place.PlacesListener
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class MyGeoFenceFragment : PlacesListener, GeofenceFragment(), OnMapReadyCallback {
    companion object {
        const val FRAGMENT_IDENTIFIER = "GEO_FRAGMENT"

        fun getInstance(): MyGeoFenceFragment {
            return MyGeoFenceFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_geo, container, false)
        val mapFragment =
                childFragmentManager.findFragmentById(R.id.main_branch_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return v
    }

    override fun getListenerTag(): String {
        return FRAGMENT_IDENTIFIER
    }

    /**
     * Required method of the [GoogleMap.OnMapClickListener] interface.
     */
    override fun onMapClick(position: LatLng?) {

    }

    /**
     * Required method of the [GoogleMap.OnMarkerClickListener] interface.
     */
    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null) {
//            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 14f))
        }

        // We return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false
    }

    override fun onPlacesChanged(updatedPosition: Int) {
        drawGeofences()
    }

}