package com.crepetete.transittracker.views.fragments.placelist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.constraint.ConstraintLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.crepetete.transittracker.R
import com.crepetete.transittracker.models.place.PlaceData
import com.crepetete.transittracker.models.place.PlacesController
import com.crepetete.transittracker.models.place.adapter.viewholder.adapter.PlacesAdapter
import com.crepetete.transittracker.models.fab.PlacesFabAnimator
import com.crepetete.transittracker.views.activities.main.MainActivity
import com.crepetete.transittracker.views.activities.main.MainActivity.Companion.REQUEST_PLACE_PICKER
import com.crepetete.transittracker.views.fragments.map.MyGeoFenceFragment
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import timber.log.Timber


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

    private lateinit var mFabAdd: FloatingActionButton
    private lateinit var mFabStart: FloatingActionButton
    private var mFabAnimator: PlacesFabAnimator? = null

    private lateinit var mCoordinator: ConstraintLayout
    private lateinit var mRecyclerView: RecyclerView

    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var mAdapter: PlacesAdapter
    private var mListState: Parcelable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_places, container, false)

        mRecyclerView = view.findViewById(R.id.recycler_view)
        mFabAdd = view.findViewById(R.id.fab_add)
        mFabStart = view.findViewById(R.id.fab_start)
        mCoordinator = view.findViewById(R.id.coordinator)

        mLayoutManager = LinearLayoutManager(context)
        mRecyclerView.layoutManager = mLayoutManager

        mRecyclerView.itemAnimator = DefaultItemAnimator()
        context?.let {
            mAdapter = PlacesAdapter(it, PlacesController.getPlaces().toMutableList(), mRecyclerView)
            mRecyclerView.adapter = mAdapter

            mFabAnimator = PlacesFabAnimator(it, arrayOf(mFabAdd, mFabStart), mCoordinator)
            mFabAnimator!!.showInitialFab()

            mFabAdd.setOnClickListener({
                openPlacePickerIntent()
            })

            mFabStart.setOnClickListener({
                openGeoFragment()
            })
        }
        setFabVisibility()

        return view
    }

    override fun onResume() {
        super.onResume()
        if (mListState != null) {
            mLayoutManager.onRestoreInstanceState(mListState)
        }
        mAdapter.onResume()

        setFabVisibility()
    }

    override fun onPause() {
        mAdapter.onStop()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        mAdapter.onStop()
    }

    override fun onDestroy() {
        mFabAnimator?.let {
            it.hideFabs()
            it.removeListener()
        }
        super.onDestroy()
    }

    private fun openPlacePickerIntent() {
        // Open PlacePicker Intent.
        try {
            val intentBuilder = PlacePicker.IntentBuilder()
            val intent = intentBuilder.build(activity)
            // Start the Intent by requesting a result, identified by a request code.
            startActivityForResult(intent, MainActivity.REQUEST_PLACE_PICKER)
        } catch (e: GooglePlayServicesRepairableException) {
            GoogleApiAvailability.getInstance().getErrorDialog(activity,
                    e.connectionStatusCode, 0)
        } catch (e: GooglePlayServicesNotAvailableException) {
            Toast.makeText(activity, "Google Play Services is not available",
                    Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Extracts data from PlacePicker result.
     * This method is called when an Intent has been started by calling [startActivityForResult].
     * The Intent for the [PlacePicker] is started with [REQUEST_PLACE_PICKER] request code.
     * When a result with this request code is received in this method, its data is extracted by
     * converting the Intent data to a [com.google.android.gms.location.places.Place] through the
     * [PlacePicker.getPlace] call.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PLACE_PICKER) {
            // This result is from the PlacePicker dialog.
            if (resultCode == Activity.RESULT_OK) {
                /*
                User has picked a place, extract data.
                Data is extracted from the returned intent by retrieving a Place object from the
                PlacePicker.
                 */
                PlacesController.addPlace(PlaceData(PlacePicker.getPlace(context, data)))
            } else {
                // Error message
                Timber.e("Could not receive a Place")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun openGeoFragment() {
        mFabAnimator?.hideFabs()

        activity?.supportFragmentManager?.let {
            var fragment: Fragment? =
                    it.findFragmentByTag(MyGeoFenceFragment.FRAGMENT_IDENTIFIER)
            if (fragment == null) {
                fragment = MyGeoFenceFragment.getInstance()
            }

            it.beginTransaction()
                    .add(MainActivity.fragmentContainerId, fragment, PlacePickerFragment.TAG)
                    .addToBackStack(PlacePickerFragment.TAG)
                    .commit()
        }
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

    private fun setFabVisibility() {
        mFabAnimator?.let {
            if (PlacesController.getNumberOfPlaces() == 0) {
                it.hideSecondFab()
                it.showInitialFab()
            } else {
                it.showSecondFab()
            }
        }

    }
}