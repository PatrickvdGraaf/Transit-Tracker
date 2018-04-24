package com.crepetete.transittracker.models.view

import android.content.Context
import android.support.annotation.Size
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.design.widget.FloatingActionButton
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.view.animation.OvershootInterpolator
import com.crepetete.transittracker.R

open class FabAnimator(
        context: Context,
        @Size(min = 1) private val mFabs: Array<FloatingActionButton>,
        private val mConstraintLayout: ConstraintLayout,
        private val mAnchorView: View) {

    private var mFirstFabVisible = false
    private var mSecondFabVisible = false

    private val mMargin = context.resources.getDimensionPixelOffset(R.dimen.margin_medium)

    /**
     * Animates a normal sized [FloatingActionButton] coming in from under the parent view and
     * finishing above the given viewBelow with a margin of 16.
     */
    fun showInitialFab() {
        val fab = mFabs[0]

        val constraintSet = ConstraintSet()
        constraintSet.clone(mConstraintLayout)

        constraintSet.clear(fab.id, ConstraintSet.TOP)
        constraintSet.connect(fab.id, ConstraintSet.BOTTOM, mAnchorView.id, ConstraintSet.TOP,
                mMargin)

        val transition = ChangeBounds()
        transition.interpolator = OvershootInterpolator()
        TransitionManager.beginDelayedTransition(mConstraintLayout, transition)
        constraintSet.applyTo(mConstraintLayout)

        mFirstFabVisible = true
    }

    fun showSecondFab() {
        if (mFabs.size > 1) {
            if (!mFirstFabVisible) {
                showInitialFab()
            }

            val firstFab = mFabs[0]
            val secondFab = mFabs[1]

            val constraintSet = ConstraintSet()
            constraintSet.clone(mConstraintLayout)

            constraintSet.connect(firstFab.id, ConstraintSet.BOTTOM, secondFab.id,
                    ConstraintSet.TOP)

            constraintSet.clear(secondFab.id, ConstraintSet.TOP)
            constraintSet.connect(secondFab.id, ConstraintSet.BOTTOM, mAnchorView.id,
                    ConstraintSet.TOP, mMargin)

            val transition = ChangeBounds()
            transition.interpolator = OvershootInterpolator()
            TransitionManager.beginDelayedTransition(mConstraintLayout, transition)
            firstFab.size = FloatingActionButton.SIZE_MINI
            constraintSet.applyTo(mConstraintLayout)

            mSecondFabVisible = true
        }
    }

    fun hideFabs() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mConstraintLayout)

        val firstFab = mFabs[0]
        val secondFab = mFabs[1]

        if (mFabs.size > 1 && mSecondFabVisible) {
            constraintSet.clear(secondFab.id, ConstraintSet.TOP)
            constraintSet.clear(secondFab.id, ConstraintSet.BOTTOM)
            constraintSet.connect(secondFab.id, ConstraintSet.TOP, mConstraintLayout.id,
                    ConstraintSet.BOTTOM)
        }

        constraintSet.clear(firstFab.id, ConstraintSet.BOTTOM)
        constraintSet.connect(firstFab.id, ConstraintSet.TOP, mConstraintLayout.id,
                ConstraintSet.BOTTOM)

        val transition = ChangeBounds()
        transition.interpolator = OvershootInterpolator()
        TransitionManager.beginDelayedTransition(mConstraintLayout, transition)
        firstFab.size = FloatingActionButton.SIZE_NORMAL
        constraintSet.applyTo(mConstraintLayout)

        mFirstFabVisible = false
        mSecondFabVisible = false
    }

    fun hideSecondFab() {
        if (mSecondFabVisible && mFabs.size > 1) {
            val firstFab = mFabs[0]
            val secondFab = mFabs[1]

            val constraintSet = ConstraintSet()
            constraintSet.clone(mConstraintLayout)

            constraintSet.connect(firstFab.id, ConstraintSet.BOTTOM, mAnchorView.id,
                    ConstraintSet.TOP, mMargin)

            constraintSet.clear(secondFab.id, ConstraintSet.BOTTOM)
            constraintSet.connect(secondFab.id, ConstraintSet.TOP, mConstraintLayout.id,
                    ConstraintSet.BOTTOM)

            val transition = ChangeBounds()
            transition.interpolator = OvershootInterpolator()
            TransitionManager.beginDelayedTransition(mConstraintLayout, transition)
            constraintSet.applyTo(mConstraintLayout)

            mSecondFabVisible = false
            firstFab.size = FloatingActionButton.SIZE_NORMAL
        }
    }
}