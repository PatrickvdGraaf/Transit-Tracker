package com.crepetete.transittracker.activities.main.fragments.map.cardstream

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.Point
import android.view.View
import android.view.WindowManager
import android.view.animation.BounceInterpolator

class DefaultCardStreamAnimator : CardStreamAnimator() {
    /**
     * Defines initial animation of each child which is fired then a user rotates a screen.
     *
     * @param context
     * @return ObjectAnimator for initial animation
     */
    override fun getInitialAnimator(context: Context): ObjectAnimator? {
        return ObjectAnimator.ofPropertyValuesHolder(Object(),
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.5f, 1f),
                PropertyValuesHolder.ofFloat(View.ROTATION, 60f, 0f))
                .setDuration((200 * mSpeedFactor).toLong())
    }

    /**
     * Defines disappearing animation of a child which is fires when a view is removed
     * programmatically.
     *
     * @param context
     * @return ObjectAnimator for disappearing animation.
     */
    override fun getDisappearingAnimator(context: Context): ObjectAnimator? {
        return ObjectAnimator.ofPropertyValuesHolder(Object(),
                PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0f),
                PropertyValuesHolder.ofFloat(View.ROTATION, 0f, 270f))
                .setDuration((200 * mSpeedFactor).toLong())
    }

    /**
     * Defines appearing animation of a child which is fired when a view is added
     * programmatically.
     *
     * @param context
     * @return ObjectAnimator for appearing animation.
     */
    override fun getAppearingAnimator(context: Context): ObjectAnimator? {
        val outPoint = Point()
        val wm: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getSize(outPoint)

        return ObjectAnimator.ofPropertyValuesHolder(Object(),
                PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, outPoint.y / 2f, 0f),
                PropertyValuesHolder.ofFloat(View.ROTATION, -45f, 0f))
                .setDuration((200 * mSpeedFactor).toLong())
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
    override fun getSwipeInAnimator(view: View?, deltaX: Float, deltaY: Float): ObjectAnimator? {
        val deltaXAbs = Math.abs(deltaX)

        val fractionCovered = 1f - (deltaXAbs / (view?.width ?: 0))
        val duration: Long = Math.abs(((1 - fractionCovered) * 200 * mSpeedFactor).toInt()).toLong()

        // Animate position and alpha of swiped item

        val animator = ObjectAnimator.ofPropertyValuesHolder(Object(),
                PropertyValuesHolder.ofFloat(View.ALPHA, 1f),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0f),
                PropertyValuesHolder.ofFloat(View.ROTATION_Y, 0f))
        animator.setDuration(duration).interpolator = BounceInterpolator()
        return animator
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
    override fun getSwipeOutAnimation(view: View?, deltaX: Float, deltaY: Float): ObjectAnimator? {
        val endX: Float = if (deltaX < 0) {
            -view?.width!!.toFloat()
        } else {
            view?.width!!.toFloat()
        }
        val endRotationY: Float = if (deltaX > 0) {
            -15f
        } else {
            15f
        }

        val deltaXAbs = Math.abs(deltaX)
        val fractionCovered: Float = 1f - (deltaXAbs / view.width)
        val duration: Long = Math.abs(((1 - fractionCovered) * 200 * mSpeedFactor).toInt()).toLong()

        return ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0f),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_X, endX),
                PropertyValuesHolder.ofFloat(View.ROTATION_Y, endRotationY)).setDuration(duration)
    }
}