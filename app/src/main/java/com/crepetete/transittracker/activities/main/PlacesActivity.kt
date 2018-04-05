package com.crepetete.transittracker.activities.main

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import com.crepetete.transittracker.R
import com.crepetete.transittracker.activities.main.fragments.PlacePickerFragment
import com.crepetete.transittracker.activities.main.fragments.map.cardstream.*
import java.util.*

class PlacesActivity : FragmentActivity(), CardStream {
    companion object {
        const val FRAG_TAG = "PlacePickerFragment"
        const val RETENTION_TAG = "retention"
    }

    private var mCardStreamFragment: CardStreamFragment? = null
    private var mRetentionFragment: StreamRetentionFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places)

        val fm = supportFragmentManager
        var fragment = fm.findFragmentByTag(FRAG_TAG)

        if (fragment == null) {
            val transaction = fm.beginTransaction()
            fragment = PlacePickerFragment()
            transaction.add(fragment, FRAG_TAG)
            transaction.commit()
        }

        // Use fragment as click listener for cards, but must implement correct interface.
        if (fragment !is OnCardClickListener) {
            throw ClassCastException("PlacePickerFragment must implement OnCardClickListener " +
                    "interface")
        }

        val clickListener = fragment as OnCardClickListener

        mRetentionFragment = fm.findFragmentByTag(RETENTION_TAG) as StreamRetentionFragment?
        if (mRetentionFragment == null) {
            mRetentionFragment = StreamRetentionFragment()
            fm.beginTransaction().add(mRetentionFragment, RETENTION_TAG).commit()
        } else {
            // If the retention fragment already existed, we need to pull some state
            val state = mRetentionFragment!!.getCardStream()

            // Dump it in CardStreamFragment
            mCardStreamFragment = fm.findFragmentById(R.id.fragment_cardstream)
                    as CardStreamFragment
            if (mCardStreamFragment != null) {
                mCardStreamFragment!!.restoreState(state, clickListener)
            }
        }
    }

    override fun getCardStream(): CardStreamFragment {
        if (mCardStreamFragment == null) {
            mCardStreamFragment = supportFragmentManager.findFragmentById(R.id.fragment_cardstream)
                    as CardStreamFragment?
            if (mCardStreamFragment == null) {
                throw MissingResourceException("PlacesActivity should have a fragment with id " +
                        "R.id.fragment_cardsteam in its layout file", this.javaClass.canonicalName,
                        "R.id.fragment_cardstream")
            }
        }
        return mCardStreamFragment!!
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        val state: CardStreamState? = getCardStream().dumpState()
        if (state != null) {
            mRetentionFragment?.storeCardStream(state)
        }
    }
}
