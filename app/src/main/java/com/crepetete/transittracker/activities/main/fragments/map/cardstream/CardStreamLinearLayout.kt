package com.crepetete.transittracker.activities.main.fragments.map.cardstream

import android.animation.Animator
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import com.crepetete.transittracker.R
import timber.log.Timber

/**
 * A Layout that contains a stream of card views.
 */
open class CardStreamLinearLayout : LinearLayout {
    companion object {
        const val ANIMATION_SPEED_SLOW = 1001
        const val ANIMATION_SPEED_NORMAL = 1002
        const val ANIMATION_SPEED_FAST = 1003
    }

    private val mFixedViewList = arrayListOf<View>()
    private val mChildRect = Rect()
    private var mAnimators: CardStreamAnimator? = null
    private var mDismissListener: OnDismissListener? = null
    private var mLayouted = false
    private var mSwiping = false
    private var mFirstVisibleCardTag: String? = null
    private var mShowInitialAnimation = false
    private var mSwipeSlop = -1
    private var mLastDownX = 0

    /**
     * Handle touch events to fade/move dragged items as they are swiped out
     */
    private val mTouchListener: OnTouchListener = object : OnTouchListener {
        private var mDownX: Float = 0f
        private var mDownY: Float = 0f

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (event != null) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mDownX = event.x
                        mDownY = event.y
                        v?.performClick()
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        resetAnimatedView(v)
                        mSwiping = false
                        mDownX = 0f
                        mDownY = 0f
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val x = event.x + (v?.translationX ?: 0) as Float
                        val y = event.y + (v?.translationY ?: 0) as Float

                        mDownX = if (mDownX == 0f) x else mDownX
                        mDownY = if (mDownY == 0f) y else mDownY

                        val deltaX = x - mDownX
                        val deltaY = y - mDownY

                        if (!mSwiping && isSwiping(deltaX, deltaY)) {
                            mSwiping = true
                            v?.parent?.requestDisallowInterceptTouchEvent(true)
                        } else {
                            swipeView(v, deltaX, deltaY)
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        // User let go - figure out whether to animate the view out, or back into
                        // place.
                        if (mSwiping) {
                            val x = event.x + (v?.translationX ?: 0) as Float
                            val y = event.y + (v?.translationY ?: 0) as Float

                            val deltaX = x - mDownX
                            val deltaY = y - mDownY
                            val deltaXAbs = Math.abs(deltaX)

                            // User let go - figure out whether to animate the view out, or back
                            // into place.
                            val remove = deltaXAbs > (v?.width ?: 0) / 4 && !isFixedView(v)
                            if (remove) {
                                handleViewSwipingOut(v, deltaX, deltaY)
                            } else {
                                handleViewSwipingIn(v, deltaX, deltaY)
                            }
                            mDownX = 0f
                            mDownY = 0f
                            mSwiping = false
                        }
                    }
                }
            }
            return false
        }
    }

    /**
     * Handle end-transition animation event of each child and launch a following animation.
     */
    private val mTransitionListener = object : LayoutTransition.TransitionListener {
        /**
         * This event is sent to listeners when any type of transition animation begins.
         *
         * @param transition The LayoutTransition sending out the event.
         * @param container The ViewGroup on which the transition is playing.
         * @param view The View object being affected by the transition animation.
         * @param transitionType The type of transition that is beginning,
         * [android.animation.LayoutTransition.APPEARING],
         * [android.animation.LayoutTransition.DISAPPEARING],
         * [android.animation.LayoutTransition.CHANGE_APPEARING], or
         * [android.animation.LayoutTransition.CHANGE_DISAPPEARING].
         */
        override fun startTransition(transition: LayoutTransition?, container: ViewGroup?,
                                     view: View?, transitionType: Int) {
            Timber.d("Start LayoutTransition animation: $transitionType")
        }

        /**
         * This event is sent to listeners when any type of transition animation ends.
         *
         * @param transition The LayoutTransition sending out the event.
         * @param container The ViewGroup on which the transition is playing.
         * @param view The View object being affected by the transition animation.
         * @param transitionType The type of transition that is ending,
         * [android.animation.LayoutTransition.APPEARING],
         * [android.animation.LayoutTransition.DISAPPEARING],
         * [android.animation.LayoutTransition.CHANGE_APPEARING], or
         * [android.animation.LayoutTransition.CHANGE_DISAPPEARING].
         */
        override fun endTransition(transition: LayoutTransition?, container: ViewGroup?,
                                   view: View?, transitionType: Int) {
            Timber.d("End LayoutTransition animation $transitionType")
            if (transitionType == LayoutTransition.APPEARING) {
                val area = view?.findViewById<View?>(R.id.card_actionarea)
                if (area != null) {
                    runShowActionAreaAnimation(container, area)
                }
            }
        }
    }

    /**
     * Handle a hierarchy change event.
     * When a new child is added, scroll to bottom and hide action area.
     */
    private val mOnHierarchyChangeListener = object : OnHierarchyChangeListener {
        /**
         * Called when a new child is added to a parent view.
         *
         * @param parent the view in which a child was added
         * @param child the new child view added in the hierarchy
         */
        override fun onChildViewAdded(parent: View?, child: View?) {
            Timber.d("Child is added $child")

            (parent?.parent as ScrollView?)?.fullScroll(FOCUS_DOWN)

            if (layoutTransition != null) {
                val view = child?.findViewById<View?>(R.id.card_actionarea)
                if (view != null) {
                    view.alpha = 0f
                }
            }
        }

        /**
         * Called when a child is removed from a parent view.
         *
         * @param parent the view from which the child was removed
         * @param child the child removed from the hierarchy
         */
        override fun onChildViewRemoved(parent: View?, child: View?) {
            Timber.d("Child is removed: $child")
            mFixedViewList.remove(child)
        }
    }

    constructor(context: Context) : super(context) {
        initialize(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs,
            defStyle) {
        initialize(attrs, defStyle)
    }

    /**
     * Add a card view with a canDismiss flag
     *
     * @param cardView   A card view
     * @param canDismiss Flag to inficate this card is dismissible or not
     */
    fun addCard(cardView: View, canDismiss: Boolean) {
        if (cardView.parent == null) {
            initCard(cardView, canDismiss)

            var param = cardView.layoutParams
            if (param != null) {
                param = generateDefaultLayoutParams()
            }
            super.addView(cardView, -1, param)
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child.parent == null) {
            initCard(child, true)
            super.addView(child, index, params)
        }
    }

    public override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        Timber.d("onLayout $changed")

        if (changed && !mLayouted) {
            mLayouted = true

            var animator: ObjectAnimator? = mAnimators?.getDisappearingAnimator(context)
            val layoutTransition = LayoutTransition()

            layoutTransition.setAnimator(LayoutTransition.DISAPPEARING, animator)

            animator = mAnimators?.getAppearingAnimator(context)
            layoutTransition.setAnimator(LayoutTransition.APPEARING, animator)

            layoutTransition.addTransitionListener(mTransitionListener)

            if (animator != null) {
                layoutTransition.setDuration(animator.duration)
            }

            setLayoutTransition(layoutTransition)

            if (mShowInitialAnimation) {
                runInitialAnimations()
            }

            if (mFirstVisibleCardTag != null) {
                scrollToCard(mFirstVisibleCardTag!!)
                mFirstVisibleCardTag = null
            }
        }
    }

    /**
     * Check whether a user moved enough distance to start a swipe action or not.
     *
     * @param deltaX
     * @param deltaY
     * @return true if a user is swiping.
     */
    protected fun isSwiping(deltaX: Float, deltaY: Float): Boolean {
        if (mSwipeSlop < 0) {
            // Get swiping slop from ViewConfiguration
            mSwipeSlop = ViewConfiguration.get(context).scaledTouchSlop
        }

        val swiping = false
        val absDeltaX = Math.abs(deltaX)

        if (absDeltaX > mSwipeSlop) {
            return true
        }

        return swiping
    }

    /**
     * Swipe a view by moving distance
     *
     * @param child a target view
     * @param x moving distance by x-axis.
     * @param deltaY y moving distance by y-axis.
     */
    protected fun swipeView(child: View?, x: Float, deltaY: Float) {
        if (child == null) {
            return
        }

        var deltaX = x
        if (isFixedView(child)) {
            deltaX /= 4
        }

        val deltaXAbs = Math.abs(deltaX)
        val fractionCovered = deltaXAbs / child.width.toFloat()

        child.translationX = deltaX
        child.alpha = 1f - fractionCovered

        if (deltaX > 0) {
            child.rotationY = -15f * fractionCovered
        } else {
            child.rotationY = 15f * fractionCovered
        }
    }

    protected fun notifyOnDismissEvent(child: View?) {
        if (child == null || mDismissListener == null) {
            return
        }
        mDismissListener!!.onDismiss(child.tag.toString())
    }

    /**
     * Get the tag of the first visible child in this layout
     *
     * @return tag of the first visible child, or null
     */
    fun getFirstVisibleCardTag(): String? {
        val count = childCount

        if (count == 0) {
            return null
        }

        for (index in 0 until count) {
            // Check the position of each view.
            val child = getChildAt(index)
            if (child.getGlobalVisibleRect(mChildRect)) {
                return child.tag.toString()
            }
        }
        return null
    }

    /**
     * Set the first visible card of this linear layout.
     *
     * @param tag of a card which should already added to this layout.
     */
    fun setFirstVisibleCard(tag: String?) {
        if (tag == null) {
            // Do nothing.
            return
        }

        if (mLayouted) {
            scrollToCard(tag)
        } else {
            // Keep the tag for next use.
            mFirstVisibleCardTag = tag
        }
    }

    /**
     * If this flag is set, after finishing initial onLayout event, an initial animation, which is
     * defined in {@link DefaultCardStreamAnimator}, is launched.
     */
    fun triggerShowInitialAnimation() {
        mShowInitialAnimation = true
    }

    fun setCardStreamAnimator(animators: CardStreamAnimator?) {
        mAnimators = animators ?: CardStreamAnimator.Companion.EmptyAnimator()
        val layoutTransition = layoutTransition
        if (layoutTransition != null && mAnimators != null) {
            layoutTransition.setAnimator(LayoutTransition.APPEARING,
                    mAnimators!!.getAppearingAnimator(context))
            layoutTransition.setAnimator(LayoutTransition.DISAPPEARING,
                    mAnimators!!.getDisappearingAnimator(context))
        }
    }

    fun setOnDismissListener(listener: OnDismissListener){
        mDismissListener = listener
    }

    /**
     * Set a OnDismissListener which called when the user
     */
    private fun initialize(attrs: AttributeSet?, defStyle: Int) {
        var speedFactor = 1f

        if (attrs != null) {
            val a: TypedArray? = context.obtainStyledAttributes(attrs,
                    R.styleable.CardStreamLinearLayout,
                    defStyle,
                    0)
            if (a != null) {
                val speedType = a.getInt(R.styleable.CardStreamLinearLayout_animationDuration,
                        ANIMATION_SPEED_SLOW)
                when (speedType) {
                    ANIMATION_SPEED_FAST -> speedFactor = 0.5f
                    ANIMATION_SPEED_NORMAL -> speedFactor = 1f
                    ANIMATION_SPEED_SLOW -> speedFactor = 2f
                }

                val animatorName = a.getString(R.styleable.CardStreamLinearLayout_animators)

                try {
                    if (animatorName != null) {
                        mAnimators = javaClass.classLoader.loadClass(animatorName).newInstance()
                                as CardStreamAnimator
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Fail to load animator: $animatorName")
                } finally {
                    if (mAnimators == null) {
                        mAnimators = DefaultCardStreamAnimator()
                    }
                }
            }
            a?.recycle()
        }
        mAnimators?.setSpeedFactor(speedFactor)
        mSwipeSlop = ViewConfiguration.get(context).scaledTouchSlop
        setOnHierarchyChangeListener(mOnHierarchyChangeListener)
    }

    private fun initCard(cardView: View, canDismiss: Boolean) {
        resetAnimatedView(cardView)
        cardView.setOnTouchListener(mTouchListener)
        if (!canDismiss) {
            mFixedViewList.add(cardView)
        }
    }

    private fun isFixedView(v: View?): Boolean {
        return mFixedViewList.contains(v)
    }

    private fun resetAnimatedView(child: View?) {
        if (child == null) {
            return
        }
        child.alpha = 1f
        child.translationX = 0f
        child.translationY = 0f
        child.rotation = 0f
        child.rotationX = 0f
        child.rotationY = 0f
        child.scaleX = 1f
        child.scaleY = 1f
    }

    private fun runInitialAnimations() {
        if (mAnimators == null) {
            return
        }

        for (index in 0 until childCount) {
            val child = getChildAt(index)
            val animator = mAnimators!!.getInitialAnimator(context)
            if (animator != null) {
                animator.target = child
                animator.start()
            }
        }
    }

    private fun runShowActionAreaAnimation(parent: View?, area: View) {
        area.pivotY = 0f
        area.pivotX = (parent?.width ?: 0) / 2f

        area.alpha = 0.5f
        area.rotationX = -90f
        area.animate().rotationX(0f).alpha(1f).duration = 400
    }

    private fun handleViewSwipingOut(child: View?, deltaX: Float, deltaY: Float) {
        val animator = mAnimators?.getSwipeOutAnimation(child, deltaX, deltaY)
        if (animator != null) {
            animator.addListener(object : EndAnimationWrapper() {
                override fun onAnimationEnd(animation: Animator?) {
                    removeView(child)
                    notifyOnDismissEvent(child)
                }
            })
        } else {
            removeView(child)
            notifyOnDismissEvent(child)
        }

        if (animator != null) {
            animator.target = child
            animator.start()
        }
    }

    private fun handleViewSwipingIn(child: View?, deltaX: Float, deltaY: Float) {
        val animator = mAnimators?.getSwipeInAnimator(child, deltaX, deltaY)
        if (animator != null) {
            animator.addListener(object : EndAnimationWrapper() {
                override fun onAnimationEnd(animation: Animator?) {
                    if (child != null) {
                        child.translationX = 0f
                        child.translationY = 0f
                    }
                }
            })
        } else {
            if (child != null) {
                child.translationX = 0f
                child.translationY = 0f
            }
        }

        if (animator != null) {
            animator.target = child
            animator.start()
        }
    }

    private fun scrollToCard(tag: String) {
        val count = childCount
        for (index in 0 until count) {
            val child = getChildAt(index)

            if (tag == child.tag) {
                val parent = parent
                if (parent != null && parent is ScrollView) {
                    parent.smoothScrollTo(0, child.top - paddingTop - child.paddingTop)
                }
                return
            }
        }
    }

    interface OnDismissListener {
        fun onDismiss(tag: String)
    }

    /**
     * Empty default AnimationListener
     */
    private abstract class EndAnimationWrapper : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationEnd(animation: Animator?) {
        }

        override fun onAnimationRepeat(animation: Animator?) {
        }
    }
}