package com.crepetete.transittracker.views.activities.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.widget.Toast
import com.crepetete.transittracker.R
import com.crepetete.transittracker.config.AnimationHelper
import com.crepetete.transittracker.config.bind
import com.crepetete.transittracker.models.intent.service.GeofenceService
import com.crepetete.transittracker.models.place.ParcelablePlace
import com.crepetete.transittracker.models.place.PlacesController
import com.crepetete.transittracker.models.view.PlacesFabAnimator
import com.crepetete.transittracker.views.fragments.geo.MyGeoFenceFragment
import com.crepetete.transittracker.views.fragments.home.HomeFragment
import com.crepetete.transittracker.views.fragments.place.PlacePickerFragment
import com.crepetete.transittracker.views.fragments.settings.SettingsFragment
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlacePicker
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber


class MainActivity : FragmentActivity() {
    companion object {
        const val TAG_SAVED_FRAG = "MyTravelsFragment"
        const val TAG_SETTINGS_FRAG = "SettingsFragment"

        /**
         * Request code passed to the PlacePicker intent to identify its result when it returns.
         */
        private const val REQUEST_PLACE_PICKER = 1

        @IdRes
        const val fragmentContainerId: Int = R.id.fragment_container
    }

    private val mFragmentManager: FragmentManager by lazy { supportFragmentManager }

    private lateinit var mFabAnimator: PlacesFabAnimator

    private val mOnNavigationItemSelectedListener = BottomNavigationView
            .OnNavigationItemSelectedListener { item ->
                val tag: String
                var fragment: Fragment?

                if (item.itemId != R.id.navigation_dashboard) {
                    mFabAnimator.hideFabs()
                }

                when (item.itemId) {
                    R.id.navigation_home -> {
                        tag = TAG_SAVED_FRAG

                        fragment = mFragmentManager.findFragmentByTag(tag)
                        if (fragment == null) {
                            fragment = HomeFragment()
                        }
                        mFragmentManager.beginTransaction()
                                .replace(fragmentContainerId, fragment, tag)
                                .commit()
                        mFragmentManager.executePendingTransactions()
                    }
                    R.id.navigation_dashboard -> {
                        openPlacesFragment()
                    }
                    R.id.navigation_notifications -> {
                        tag = TAG_SETTINGS_FRAG

                        fragment = mFragmentManager.findFragmentByTag(tag)
                        if (fragment == null) {
                            fragment = SettingsFragment()
                        }
                        mFragmentManager.beginTransaction()
                                .replace(fragmentContainerId, fragment, tag)
                                .commit()
                        mFragmentManager.executePendingTransactions()
                    }
                }
                return@OnNavigationItemSelectedListener true
            }

    private val mFabAdd by bind<FloatingActionButton>(R.id.fab_add)
    private val mFabStart by bind<FloatingActionButton>(R.id.fab_start)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        mOnNavigationItemSelectedListener.onNavigationItemSelected(navigation.menu.getItem(1))
        navigation.menu.getItem(1).isChecked = true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setFabVisibility()
    }

    override fun onStart() {
        super.onStart()
        mFabAnimator = PlacesFabAnimator(this, arrayOf(mFabAdd, mFabStart), coordinator,
                navigation)
        mFabAnimator.showInitialFab()

        mFabAdd.setOnClickListener({
            openPlacePickerIntent()
        })
    }

    override fun onDestroy() {
        mFabAnimator.removeListener()
        stopService(Intent(this, GeofenceService::class.java))
        super.onDestroy()
    }

    private fun setFabVisibility() {
        if (PlacesController.getNumberOfPlaces() == 0) {
            mFabAnimator.hideSecondFab()
        } else {
            mFabAnimator.showSecondFab()
        }
    }

    private fun openPlacePickerIntent() {
        // Open PlacePicker Intent.
        try {
            val intentBuilder = PlacePicker.IntentBuilder()
            val intent = intentBuilder.build(this)
            // Start the Intent by requesting a result, identified by a request code.
            startActivityForResult(intent, REQUEST_PLACE_PICKER)
        } catch (e: GooglePlayServicesRepairableException) {
            GoogleApiAvailability.getInstance().getErrorDialog(this,
                    e.connectionStatusCode, 0)
        } catch (e: GooglePlayServicesNotAvailableException) {
            Toast.makeText(this, "Google Play Services is not available",
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
                val place = PlacePicker.getPlace(this, data)
                PlacesController.addPlace(ParcelablePlace.fromPlace(place))
            } else {
                // Error message
                Timber.e("Could not receive a Place")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun openPlacesFragment() {
        mFragmentManager.executePendingTransactions()
        var fragment: Fragment? = mFragmentManager.findFragmentByTag(PlacePickerFragment.TAG)
        if (fragment == null) {
            fragment = PlacePickerFragment.getInstance()


            mFabStart.setOnClickListener({
                openGeoFragment()
            })
            mFragmentManager.beginTransaction().replace(fragmentContainerId, fragment,
                    PlacePickerFragment.TAG)
                    .addToBackStack(PlacePickerFragment.TAG).commit()
            return
        }

        mFragmentManager.beginTransaction()
                .replace(fragmentContainerId, fragment, PlacePickerFragment.TAG)
                .addToBackStack(PlacePickerFragment.TAG)
                .commit()

        setFabVisibility()
    }

    private fun openGeoFragment() {
        mFabAnimator.hideFabs()

        var fragment: Fragment? =
                mFragmentManager.findFragmentByTag(MyGeoFenceFragment.FRAGMENT_IDENTIFIER)
        if (fragment == null) {
            fragment = MyGeoFenceFragment.getInstance()
        }

        mFragmentManager.beginTransaction()
                .add(fragmentContainerId, fragment, PlacePickerFragment.TAG)
                .addToBackStack(PlacePickerFragment.TAG)
                .commit()
    }

    private fun showBottomNavBar() {
        navigation.animate().translationY(0f).duration = AnimationHelper.QUICK
    }

    private fun hideBottomNavBar() {
        navigation.animate().translationY((resources.getDimensionPixelOffset(
                R.dimen.bottom_nav_bar_height)).toFloat()).duration = AnimationHelper.QUICK
    }
}
