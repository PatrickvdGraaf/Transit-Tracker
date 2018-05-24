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
                if (item.itemId != R.id.navigation_dashboard) {
                }

                when (item.itemId) {
                    R.id.navigation_home -> {
                        openSavesFragment()
                    }
                    R.id.navigation_dashboard -> {
                        openPlacesFragment()
                    }
                    R.id.navigation_notifications -> {
                        openPreferenceFragment()
                    }
                }
                return@OnNavigationItemSelectedListener true
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        mOnNavigationItemSelectedListener.onNavigationItemSelected(mNavigation.menu.getItem(1))
        mNavigation.menu.getItem(1).isChecked = true
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        PlacesController.onStart(this)
    }

    override fun onDestroy() {
        stopService(Intent(this, GeofenceService::class.java))
        PlacesController.onStop()
        super.onDestroy()
    }

    private fun openSavesFragment() {
        var fragment = mFragmentManager.findFragmentByTag(TAG_SAVED_FRAG)
        if (fragment == null) {
            fragment = ItemFragment()
        }
        mFragmentManager.beginTransaction()
                .replace(fragmentContainerId, fragment, TAG_SAVED_FRAG)
                .commit()
        mFragmentManager.executePendingTransactions()
    }

    private fun openPlacesFragment() {
        mFragmentManager.executePendingTransactions()
        var fragment: Fragment? = mFragmentManager.findFragmentByTag(PlacePickerFragment.TAG)
        if (fragment == null) {
            fragment = PlacePickerFragment.getInstance()
        }
        mFragmentManager.beginTransaction()
                .replace(fragmentContainerId, fragment, PlacePickerFragment.TAG)
                .addToBackStack(PlacePickerFragment.TAG)
                .commit()
    }

    private fun openPreferenceFragment() {
        var fragment = mFragmentManager.findFragmentByTag(TAG_SETTINGS_FRAG)
        if (fragment == null) {
            fragment = SettingsFragment()
        }
        mFragmentManager.beginTransaction()
                .replace(fragmentContainerId, fragment, TAG_SETTINGS_FRAG)
                .commit()
        mFragmentManager.executePendingTransactions()
    }

    private fun showBottomNavBar() {
        mNavigation.animate().translationY(0f).duration = AnimationHelper.QUICK
    }

    private fun hideBottomNavBar() {
        mNavigation.animate().translationY((resources.getDimensionPixelOffset(
                R.dimen.bottom_nav_bar_height)).toFloat()).duration = AnimationHelper.QUICK
    }
}
