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
import com.crepetete.transittracker.models.database.DatabaseWorkerThread
import com.crepetete.transittracker.models.database.PlaceDatabase
import com.crepetete.transittracker.models.place.ParcelablePlace
import com.crepetete.transittracker.models.place.PlaceData
import com.crepetete.transittracker.models.place.adapter.viewholder.adapter.PlacesAdapter
import timber.log.Timber

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ItemFragment.OnListFragmentInteractionListener] interface.
 */
class ItemFragment : Fragment() {
    private var mDatabase: PlaceDatabase? = null

    private lateinit var mDbWorkerThread: DatabaseWorkerThread

    private val mUiHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mDbWorkerThread = DatabaseWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)

        val c = context
        if (c != null) {
            mDatabase = PlaceDatabase.getInstance(c)

            val task = Runnable {
                val placeData = mDatabase?.placeDataDao()?.getAll()
                mUiHandler.post({
                    if (placeData == null || placeData.isEmpty()) {
                        Toast.makeText(c, "No data in cache..!!", Toast.LENGTH_SHORT).show()
                    } else {
                        onPlacesReceived(view, placeData)
                    }
                })
            }
            mDbWorkerThread.postTask(task)
        }

        return view
    }

    private fun onPlacesReceived(view: View, placeData: List<PlaceData>) {
        Timber.d("Data from SQLite: ${placeData.size} items.")

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager =  LinearLayoutManager(context)
                adapter = PlacesAdapter(context, placeData.map { ParcelablePlace(it) },
                        true)
            }
        }
    }

    override fun onDestroy() {
        PlaceDatabase.destroyInstance()
        mDbWorkerThread.quit()
        super.onDestroy()
    }
}
