package com.crepetete.transittracker.views.fragments.place

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.crepetete.transittracker.R
import com.crepetete.transittracker.models.place.PlacesController
import com.crepetete.transittracker.models.place.adapter.viewholder.adapter.PlacesAdapter
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
        private const val KEY_LIST_STATE = "KEY_LIST_STATE"

        fun getInstance(): PlacePickerFragment {
            return PlacePickerFragment()
        }
    }

    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var mAdapter: PlacesAdapter
    private var mListState: Parcelable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_places, container, false)

        val mRecyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)

        mLayoutManager = LinearLayoutManager(context)
        mRecyclerView.layoutManager = mLayoutManager

        mRecyclerView.itemAnimator = DefaultItemAnimator()
        mAdapter = context?.let { PlacesAdapter(it, PlacesController.getPlaces()) }!!
        mRecyclerView.adapter = mAdapter

        return view
    }

    override fun onResume() {
        super.onResume()

        if (mListState != null) {
            mLayoutManager.onRestoreInstanceState(mListState)
        }
        mAdapter.onResume()
    }

    override fun onStop() {
        super.onStop()
        mAdapter.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mListState = mLayoutManager.onSaveInstanceState()
        outState.putParcelable(KEY_LIST_STATE, mListState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(KEY_LIST_STATE)
        }
    }
}