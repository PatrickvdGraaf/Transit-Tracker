package com.crepetete.transittracker.activities.main.fragments.map.cardstream

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button

class CardActionButton : Button {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int)
            : super(context, attrs, defStyle)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isPressed = true
                    animate().scaleX(0.98f).scaleY(0.98f).setDuration(100)
                            .interpolator = DecelerateInterpolator()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isPressed = false
                    animate().scaleX(1f).scaleY(1f).setDuration(50)
                            .interpolator = BounceInterpolator()
                }
            }
            performClick()
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        // Calls the super implementation, which generates an AccessibilityEvent
        // and calls the onClick() listener on the view, if any
        super.performClick()

        // Handle the action for the custom click here
        return true
    }
}