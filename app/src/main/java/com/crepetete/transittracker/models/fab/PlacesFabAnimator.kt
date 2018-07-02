package com.crepetete.transittracker.models.fab

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.design.widget.FloatingActionButton
import com.crepetete.transittracker.models.place.PlacesController
import com.crepetete.transittracker.models.place.PlacesListener

class PlacesFabAnimator(context: Context, mFabs: Array<FloatingActionButton>,
                        mConstraintLayout: ConstraintLayout)
    : PlacesListener, FabAnimator(context, mFabs, mConstraintLayout) {
    init {
        PlacesController.addListener(this)
    }

    override fun getListenerTag(): String {
        return "PlacesFabAnimator"
    }

    override fun onPlacesChanged(updatedPosition: Int) {
        checkFabVisibilities()
    }

    override fun onPlaceRemoved(removedPosition: Int) {
        checkFabVisibilities()
    }

    fun removeListener() {
        PlacesController.removeListener(getListenerTag())
    }

    private fun checkFabVisibilities() {
        if (PlacesController.isEmpty()) {
            hideSecondFab()
        } else {
            showSecondFab()
        }
    }
}