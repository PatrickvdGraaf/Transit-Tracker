package com.crepetete.transittracker.activities.main.fragments.map.cardstream

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View

/**
 * An abstract class which defines animators for {@link CardStreamLinearLayout}
 */
abstract class CardStreamAnimator {
    companion object {
        /**
         * A simple CardStreamAnimator implementation which is used to turn animations off.
         */
        class EmptyAnimator : CardStreamAnimator() {
            /**
             * Defines initial animation of each child which is fired then a user rotates a screen.
             *
             * @param context
             * @return ObjectAnimator for initial animation
             */
            override fun getInitialAnimator(context: Context): ObjectAnimator? {
                return null
            }

            /**
             * Defines disappearing animation of a child which is fires when a view is removed
             * programmatically.
             *
             * @param context
             * @return ObjectAnimator for disappearing animation.
             */
            override fun getDisappearingAnimator(context: Context): ObjectAnimator? {
                return null
            }

            /**
             * Defines appearing animation of a child which is fired when a view is added
             * programmatically.
             *
             * @param context
             * @return ObjectAnimator for appearing animation.
             */
            override fun getAppearingAnimator(context: Context): ObjectAnimator? {
                return null
            }

            /**
             * Define swipe-in (back to the origin position) animation of a child which is fires when a view
             * is not moved enough to be removed
             *
             * @param view      target view
             * @param deltaX    delta distance by x-axis
             * @param deltaY    delta distance by y-axis
             * @return ObjectAnimator for swipe-in animation.
             */
            override fun getSwipeInAnimator(view: View?, deltaX: Float, deltaY: Float)
                    : ObjectAnimator? {
                return null
            }

            /**
             * Define swipe-out animation of a child which is fired when a view is removed by a user swipe
             * action.
             *
             * @param view      target view
             * @param deltaX    delta distance by x-axis
             * @param deltaY    delta distance by y-axis
             * @return ObjectAnimator for swipe-out animation.
             */
            override fun getSwipeOutAnimation(view: View?, deltaX: Float, deltaY: Float)
                    : ObjectAnimator? {
                return null
            }
        }
    }

    protected var mSpeedFactor = -1f

    /**
     * Set speed factor of animations. Higher value means longer duration and slow animation.
     *
     * @param speedFactor speed type 1: SLOW, 2: NORMAL, 3: FAST
     */
    fun setSpeedFactor(speedFactor: Float) {
        mSpeedFactor = speedFactor
    }

    /**
     * Defines initial animation of each child which is fired then a user rotates a screen.
     *
     * @param context
     * @return ObjectAnimator for initial animation
     */
    abstract fun getInitialAnimator(context: Context): ObjectAnimator?

    /**
     * Defines disappearing animation of a child which is fires when a view is removed
     * programmatically.
     *
     * @param context
     * @return ObjectAnimator for disappearing animation.
     */
    abstract fun getDisappearingAnimator(context: Context): ObjectAnimator?

    /**
     * Defines appearing animation of a child which is fired when a view is added
     * programmatically.
     *
     * @param context
     * @return ObjectAnimator for appearing animation.
     */
    abstract fun getAppearingAnimator(context: Context): ObjectAnimator?

    /**
     * Define swipe-in (back to the origin position) animation of a child which is fires when a view
     * is not moved enough to be removed
     *
     * @param view      target view
     * @param deltaX    delta distance by x-axis
     * @param deltaY    delta distance by y-axis
     * @return ObjectAnimator for swipe-in animation.
     */
    abstract fun getSwipeInAnimator(view: View?, deltaX: Float, deltaY: Float): ObjectAnimator?

    /**
     * Define swipe-out animation of a child which is fired when a view is removed by a user swipe
     * action.
     *
     * @param view      target view
     * @param deltaX    delta distance by x-axis
     * @param deltaY    delta distance by y-axis
     * @return ObjectAnimator for swipe-out animation.
     */
    abstract fun getSwipeOutAnimation(view: View?, deltaX: Float, deltaY: Float): ObjectAnimator?
}