package com.crepetete.transittracker.views.activities.main

import android.content.Intent
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import com.crepetete.transittracker.R
import com.crepetete.transittracker.config.AnimationHelper
import com.crepetete.transittracker.config.bind
import com.crepetete.transittracker.models.intent.service.GeofenceService
import com.crepetete.transittracker.models.place.PlacesController
import com.crepetete.transittracker.views.fragments.place.PlacePickerFragment
import com.crepetete.transittracker.views.fragments.saves.ItemFragment
import com.crepetete.transittracker.views.fragments.settings.SettingsFragment


class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG_SAVED_FRAG = "MyTravelsFragment"
        const val TAG_SETTINGS_FRAG = "SettingsFragment"

        /**
         * Request code passed to the PlacePicker intent to identify its result when it returns.
         */
        const val REQUEST_PLACE_PICKER = 1

        @IdRes
        const val fragmentContainerId: Int = R.id.fragment_container
    }

    private val mFragmentManager: FragmentManager by lazy { supportFragmentManager }
    private val mNavigation by bind<BottomNavigationView>(R.id.navigation)

    private val mOnNavigationItemSelectedListener = BottomNavigationView
            .OnNavigationItemSelectedListener { item ->
                val transaction = mFragmentManager.beginTransaction()
                when (item.itemId) {
                    R.id.navigation_home -> {
                        var fragment = mFragmentManager.findFragmentByTag(TAG_SAVED_FRAG)
                        if (fragment == null) {
                            fragment = ItemFragment()
                        }
                        transaction.replace(fragmentContainerId, fragment, TAG_SAVED_FRAG)
//                        mFragmentManager.executePendingTransactions()
                    }
                    R.id.navigation_dashboard -> {
                        mFragmentManager.executePendingTransactions()
                        var fragment: Fragment? = mFragmentManager.findFragmentByTag(PlacePickerFragment.TAG)
                        if (fragment == null) {
                            fragment = PlacePickerFragment.getInstance()
                        }
                        transaction.replace(fragmentContainerId, fragment, PlacePickerFragment.TAG)
                                .addToBackStack(PlacePickerFragment.TAG)
                    }
                    R.id.navigation_notifications -> {
                        var fragment = mFragmentManager.findFragmentByTag(TAG_SETTINGS_FRAG)
                        if (fragment == null) {
                            fragment = SettingsFragment()
                        }
                        transaction.replace(fragmentContainerId, fragment, TAG_SETTINGS_FRAG)
//                        mFragmentManager.executePendingTransactions()
                    }
                }
                transaction.commit()
                return@OnNavigationItemSelectedListener true
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PlacesController.onStart(this)

        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        mOnNavigationItemSelectedListener.onNavigationItemSelected(mNavigation.menu.getItem(1))
        mNavigation.menu.getItem(1).isChecked = true
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroy() {
        stopService(Intent(this, GeofenceService::class.java))
        PlacesController.onStop()
        super.onDestroy()
    }

    private fun showBottomNavBar() {
        mNavigation.animate().translationY(0f).duration = AnimationHelper.QUICK
    }

    private fun hideBottomNavBar() {
        mNavigation.animate().translationY((resources.getDimensionPixelOffset(
                R.dimen.bottom_nav_bar_height)).toFloat()).duration = AnimationHelper.QUICK
    }
}
