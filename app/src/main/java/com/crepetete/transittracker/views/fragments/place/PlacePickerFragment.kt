package com.crepetete.transittracker.views.fragments.place

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.crepetete.transittracker.R
import com.crepetete.transittracker.models.place.adapter.PlacesAdapter
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker


/**
 * Sample demonstrating the use of [PlacePicker].
 * This sample shows the construction of an {@link Intent} to open the PlacePicker from the
 * Google Places API for Android and select a [Place].
 *
 * This sample uses the CardStream sample template to create the UI for this demo, which is not
 * required to use the PlacePicker API. (Please see the Readme-CardStream.txt file for details.)
 */
class PlacePickerFragment : Fragment() {
    companion object {
        const val TAG = "PlacePickerFragment"

        fun getInstance(): PlacePickerFragment {
            return PlacePickerFragment()
        }
    }

    private lateinit var mRecyclerView: RecyclerView
    private var mAdapter: PlacesAdapter = PlacesAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_places, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerView = view.findViewById(R.id.recycler_view)
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.itemAnimator = DefaultItemAnimator()
        mRecyclerView.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()
        mAdapter.onResume()
    }

    override fun onStop() {
        super.onStop()
        mAdapter.onStop()
    }
}