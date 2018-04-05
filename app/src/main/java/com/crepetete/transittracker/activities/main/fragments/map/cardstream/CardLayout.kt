package com.crepetete.transittracker.activities.main.fragments.map.cardstream

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.RelativeLayout

class CardLayout : RelativeLayout {
    private var mSwiping = false
    private var mDownX = 0f
    private var mDownY = 0f
    private var mTouchSlop = 0f

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs,
            defStyle)

    init {
        // Set whether this view can receive the focus.
        this.isFocusable = true
        // This defines the relationship between this view group and its descendants when looking
        // for a view to take focus in
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        // Make sure this view doesn't draw on its own
        setWillNotDraw(false)
        isClickable = true

        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop * 2f
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            when (event.action) {
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> mSwiping = false
            }
            performClick()
        }
        return super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    if (!mSwiping) {
                        mSwiping = Math.abs(mDownX - event.x) > mTouchSlop
                    }
                }
                MotionEvent.ACTION_DOWN -> {
                    mDownX = event.x
                    mDownY = event.y
                    mSwiping = false
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    mSwiping = false
                }
            }
            return mSwiping
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun performClick(): Boolean {
        // Calls the super implementation, which generates an AccessibilityEvent
        // and calls the onClick() listener on the view, if any
        super.performClick()

        // Handle the action for the custom click here
        return true
    }
}