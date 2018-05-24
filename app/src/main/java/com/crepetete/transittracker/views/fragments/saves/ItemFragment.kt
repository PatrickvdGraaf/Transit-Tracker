package com.crepetete.transittracker.views.fragments.saves

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.crepetete.transittracker.R
import com.crepetete.transittracker.models.database.PlaceDatabase
import com.crepetete.transittracker.models.place.PlaceData
import com.crepetete.transittracker.models.place.PlacesController
import com.crepetete.transittracker.models.place.adapter.viewholder.adapter.PlacesAdapter
import timber.log.Timber

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ItemFragment.OnListFragmentInteractionListener] interface.
 */
class ItemFragment : Fragment() {
    private val mUiHandler = Handler()

    private var mPlaces = listOf<PlaceData>()

    private var mPlaceAdapter: PlacesAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        PlacesController.getAllFromDatabase({
            if (mPlaces != it) {
                mPlaces = it
                mPlaceAdapter?.notifyDataSetChanged()
                setUI(view)
            }
        })
    }

    private fun setUI(view: View) {
        mUiHandler.post({
            if (mPlaces.isEmpty()) {
                Toast.makeText(context, "No data in cache..!!", Toast.LENGTH_SHORT).show()
            } else {
                onPlacesReceived(view, mPlaces)
            }
        })
    }

    private fun onPlacesReceived(view: View, placeData: List<PlaceData>) {
        Timber.d("Data from SQLite: ${placeData.size} items.")

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                mPlaceAdapter = PlacesAdapter(context, placeData.toMutableList(), view,
                        true)

                layoutManager = LinearLayoutManager(context)
                adapter = mPlaceAdapter
            }
        }
    }

    override fun onDestroy() {
        PlaceDatabase.destroyInstance()
        super.onDestroy()
    }
}
