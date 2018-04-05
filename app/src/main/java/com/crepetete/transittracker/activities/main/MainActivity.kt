package com.crepetete.transittracker.activities.main

import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.crepetete.transittracker.R
import com.crepetete.transittracker.activities.main.fragments.HomeFragment
import com.crepetete.transittracker.activities.main.fragments.SettingsFragment
import com.crepetete.transittracker.activities.main.fragments.map.MapsFragment
import com.crepetete.transittracker.activities.main.fragments.map.cardstream.CardStreamFragment
import com.crepetete.transittracker.activities.main.fragments.map.cardstream.OnCardClickListener
import com.crepetete.transittracker.activities.main.fragments.map.cardstream.StreamRetentionFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : FragmentActivity() {
    private val mMapsFragmentTag = "MapsFragment"
    private val mRetentionTag = "retention"

    private var mFragment: Fragment? = null
    private var mRetentionFragment: StreamRetentionFragment? = null
    private var mCardStreamFragment: CardStreamFragment? = null
    private val mFragmentManager = supportFragmentManager

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                mFragment = HomeFragment()
            }
            R.id.navigation_dashboard -> {
                val mapsFragment = mFragmentManager.findFragmentByTag(mMapsFragmentTag)
                if (mapsFragment == null) {
                    val transaction = mFragmentManager.beginTransaction()
                    mFragment = MapsFragment()
                    transaction.add(mFragment, mMapsFragmentTag)
                    transaction.commit()

                    // Use fragment as click listener for cards, but must implement correct
                    // interface
                    if (mFragment !is OnCardClickListener) {
                        throw ClassCastException("MapsFragment must implement OnCardClickListener" +
                                "interface")
                    }

                    val clickListener = fragmentManager.findFragmentByTag(mMapsFragmentTag)
                            as OnCardClickListener

                    mRetentionFragment = mFragmentManager.findFragmentByTag(mRetentionTag)
                            as StreamRetentionFragment?
                    if (mRetentionFragment == null) {
                        mRetentionFragment = StreamRetentionFragment()
                        mFragmentManager
                                .beginTransaction()
                                .add(mRetentionFragment, mRetentionTag)
                                .commit()
                    } else {
                        // If the retention fragment already existed, we need to pull some state.
                        val state = mRetentionFragment!!.getCardStream()

                        // Dump it in CardStreamFragment
                        mCardStreamFragment = mFragmentManager
                                .findFragmentById(R.id.fragment_cardstream) as CardStreamFragment?
                        mCardStreamFragment?.restoreState(state, clickListener)
                    }

                    return@OnNavigationItemSelectedListener true
                }
            }
            R.id.navigation_notifications -> {
                mFragment = SettingsFragment()
            }
        }
        val transaction = mFragmentManager.beginTransaction()
        transaction.replace(R.id.container, mFragment).commit()
        return@OnNavigationItemSelectedListener true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    fun getCardStream(): CardStreamFragment {
        if (mCardStreamFragment == null) {
            mCardStreamFragment = supportFragmentManager.findFragmentById(R.id.fragment_cardstream)
                    as CardStreamFragment?
            if (mCardStreamFragment == null) {
                throw NoSuchFieldException("Could not find a CardStreamFragment " +
                        "with ID: R.id.fragment_cardstream")
            }
        }
        return mCardStreamFragment!!
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        val state = getCardStream().dumpState()
        if (mRetentionFragment == null) {
            mRetentionFragment = StreamRetentionFragment()
        }
        mRetentionFragment!!.storeCardStream(state)
    }
}
