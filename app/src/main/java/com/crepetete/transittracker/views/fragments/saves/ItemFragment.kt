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

class ItemFragment : Fragment() {
    companion object {
        private const val PLACE_LIST_KEY = "PLACE_LIST_KEY"
    }

    private val mUiHandler = Handler()

    private var mPlaces = listOf<PlaceData>()

    private var mPlaceAdapter: PlacesAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)
//        savedInstanceState?.let {
//            if (it.containsKey(PLACE_LIST_KEY)) {
//                mPlaces = it.getParcelableArrayList(PLACE_LIST_KEY)
//            }
//        }

        PlacesController.getAllFromDatabase({
            mPlaces = it
            if (it.isEmpty()) {
                Toast.makeText(context, "No data in cache..!!", Toast.LENGTH_SHORT).show()
            }

            mPlaceAdapter?.notifyDataSetChanged()
            setUI(view)
        })

        return view
    }

    private fun setUI(view: View) {
        mUiHandler.post({
            onPlacesReceived(view, mPlaces)
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

    override fun onSaveInstanceState(outState: Bundle) {
//        outState.putParcelableArray(PLACE_LIST_KEY, mPlaces.toTypedArray())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        PlaceDatabase.destroyInstance()
        super.onDestroy()
    }
}
