package com.crepetete.transittracker.activities.main.fragments.map.cardstream

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.crepetete.transittracker.R


/**
 * A Card contains a description and has a visual state. Optionally a card can also contain a title,
 * progress indicator and zero or more actions. It is constructed through the {@link Builder}
 */
class Card {
    companion object {
        const val ACTION_POSITIVE = 1
        const val ACTION_NEGATIVE = 2
        const val ACTION_NEUTRAL = 3

        const val PROGRESS_TYPE_NO_PROGRESS = 0
        const val PROGRESS_TYPE_NORMAL = 1
        const val PROGRESS_TYPE_INDETERMINATE = 2
        const val PROGRESS_TYPE_LABEL = 3

        const val CARD_STATE_NORMAL = 1
        const val CARD_STATE_FOCUSED = 2
        const val CARD_STATE_INACTIVE = 3

        const val ANIM_DURATION: Long = 400

        class Builder {
            private lateinit var mCard: Card

            /**
             * Instantiate the builder with data from a shallow clone.
             *
             * @param listener
             * @param card
             * @see Card#createShallowClone()
             */
            constructor(listener: OnCardClickListener, card: Card) {
                mCard = card
                mCard.mClickListener = listener
            }

            /**
             * Instantiate the builder with the tag of the card.
             *
             * @param listener
             * @param tag
             */
            constructor(listener: OnCardClickListener, tag: String) {
                mCard = Card()
                mCard.mTag = tag
                mCard.mClickListener = listener
            }

            fun setTitle(title: String): Builder {
                mCard.mTitle = title
                return this
            }

            fun setDescription(desc: String): Builder {
                mCard.mDescription = desc
                return this
            }

            /**
             * Add an action.
             * The type describes how this action will be displayed. Accepted values are
             * {@link #ACTION_NEUTRAL}, {@link #ACTION_POSITIVE} or {@link #ACTION_NEGATIVE}.
             *
             * @param label The text to display for this action
             * @param id Identifier for this action, supplied in the click listener
             * @param type UI style of action
             * @return
             */
            fun addAction(label: String, id: Int, type: Int): Builder {
                mCard.addAction(label, id, type)
                return this
            }

            /**
             * Override the default layout.
             * The referenced layout file has to contain the same identifiers as defined in the default
             * layout configuration.
             * @param layout
             * @return
             * @see R.layout.card
             */
            fun setLayout(@LayoutRes layout: Int): Builder {
                mCard.mLayoutId = layout
                return this
            }

            /**
             * Set the type of progress bar to display.
             * Accepted values are:
             * <ul>
             *     <li>{@link #PROGRESS_TYPE_NO_PROGRESS} disables the progress indicator</li>
             *     <li>{@link #PROGRESS_TYPE_NORMAL}
             *     displays a standard, linear progress indicator.</li>
             *     <li>{@link #PROGRESS_TYPE_INDETERMINATE} displays an indeterminate (infite)
             *     progress indicator.</li>
             *     <li>{@link #PROGRESS_TYPE_LABEL} only displays a label text in the progress area
             *     of the card.</li>
             * </ul>
             *
             * @param progressType
             * @return
             */
            fun setProgressType(progressType: Int): Builder {
                mCard.setProgressType(progressType)
                return this
            }

            fun setProgressLabel(label: String): Builder {
                // Ensure the progress layout has been initialized, use 'no progress' by default.
                if (mCard.mCardProgress == null) {
                    mCard.setProgressType(PROGRESS_TYPE_NO_PROGRESS)
                } else {
                    mCard.mCardProgress!!.label = label
                }
                return this
            }

            fun setProgrewssMaxValue(maxValue: Int): Builder {
                // Ensure the progress layout has been initialized, use 'no progress' by default.
                if (mCard.mCardProgress == null) {
                    mCard.setProgressType(PROGRESS_TYPE_NO_PROGRESS)
                } else {
                    mCard.mCardProgress!!.maxValue = maxValue
                }
                return this
            }

            fun build(activity: Activity): Card {
                val inflater = activity.layoutInflater
                // Inflating the card.
                val cardView = inflater.inflate(mCard.mLayoutId,
                        activity.findViewById(R.id.card_stream), false) as ViewGroup

                // Check that the layout contains a TextView with the card_title id.
                val titleView = cardView.findViewById(R.id.card_title) as TextView?
                if (mCard.mTitle != null && titleView != null) {
                    mCard.mTitleView = titleView
                    mCard.mTitleView!!.text = mCard.mTitle
                } else if (titleView != null) {
                    titleView.visibility = View.GONE
                }

                val descView = cardView.findViewById(R.id.card_content) as TextView?
                if (mCard.mDescription != null && descView != null) {
                    mCard.mDescView = descView
                    mCard.mDescView!!.text = mCard.mDescription
                } else if (descView != null) {
                    cardView.visibility = View.GONE
                }

                val actionArea = cardView.findViewById(R.id.card_actionarea) as ViewGroup?

                actionArea?.let {
                    // Inflate Progress
                    initializeProgressView(inflater, it)

                    // Inflate all action views.
                    initializeActionViews(inflater, cardView, actionArea)
                }

                mCard.mCardView = cardView
                mCard.mOverlayView = cardView.findViewById(R.id.card_overlay)

                return mCard
            }

            /**
             * Initialize data from the given card.
             *
             * @param card
             * @return
             * @see Card#createShallowClone()
             */
            fun cloneFromCard(card: Card): Builder {
                mCard = card.createShallowClone()
                return this
            }

            /**
             * Build the action views by inflating the appropriate layouts and setting the text and
             * values.
             *
             * @param inflater
             * @param cardView
             * @param actionArea
             */
            private fun initializeActionViews(inflater: LayoutInflater, cardView: ViewGroup,
                                              actionArea: ViewGroup) {
                if (!mCard.mCardActions.isEmpty()) {
                    // Set action area to visible only when actions are visible
                    actionArea.visibility = View.VISIBLE
                    mCard.mActionAreaView = actionArea
                }

                // Inflate all card actions
                for (action in mCard.mCardActions) {
                    val useActionLayout = when (action.type) {
                        Card.ACTION_POSITIVE -> {
                            R.layout.card_button_positive
                        }
                        Card.ACTION_NEGATIVE -> {
                            R.layout.card_button_negative
                        }
                        else -> {
                            R.layout.card_button_neutral
                        }
                    }

                    action.actionView = inflater.inflate(useActionLayout, actionArea,
                            false)
                    val actionButton = action.actionView?.findViewById(R.id.card_button) as Button?
                    if (actionButton != null) {
                        actionButton.text = action.label
                        actionButton.setOnClickListener({
                            mCard.mTag?.let { it1 -> mCard.mClickListener.onCardClick(action.id, it1) }
                        })
                    }
                    actionArea.addView(action.actionView)
                }
            }

            /**
             * Build the progress view into the given ViewGroup.
             *
             * @param inflater
             * @param actionArea
             */
            private fun initializeProgressView(inflater: LayoutInflater, actionArea: ViewGroup) {
                // Only inflate progress layout if a progress type other than NO_PROGRESS was set
                if (mCard.mCardProgress != null) {
                    // Setup progress card.
                    val progressView = inflater.inflate(R.layout.card_progress, actionArea,
                            false)
                            as View
                    val progressBar = progressView.findViewById(R.id.card_progress) as ProgressBar
                    progressBar.findViewById<TextView>(R.id.card_progress_text)
                            .text = mCard.mCardProgress!!.label
                    progressBar.max = mCard.mCardProgress!!.maxValue
                    progressBar.progress = 0
                    mCard.mCardProgress!!.progressView = progressView
                    mCard.mCardProgress!!.setProgressType(mCard.getProgressType())
                    actionArea.addView(progressView)
                }
            }

            /**
             * Represents a clickable action, accessible from the bottom of the card.
             * Fields include the label, an ID to specify the action that was performed in the
             * callback, an action type (positive, negative, neutral), and the callback.
             */
            class CardAction {
                var label: String = ""
                var id: Int = 0
                var type: Int = 0
                var actionView: View? = null

                fun createShallowClone(): CardAction {
                    val actionClone = CardAction()
                    actionClone.label = label
                    actionClone.id = id
                    actionClone.type = type

                    // Don't return the view.  Never the view (don't want to hold view references
                    // for onConfigurationChange.
                    return actionClone
                }
            }

            /**
             * Describes the progress of a {@link Card}.
             * Three types of progress are supported:
             * <ul>
             *      <li>{@link Card#PROGRESS_TYPE_NORMAL: Standard progress bar with label text</li>
             *      <li>{@link Card#PROGRESS_TYPE_INDETERMINATE}: Indeterminate progress bar with
             *          label txt</li>
             *      <li>{@link Card#PROGRESS_TYPE_LABEL}: Label only, no progress bar</li>
             * </ul>
             */
            class CardProgress {
                internal var progressType = Card.PROGRESS_TYPE_NO_PROGRESS
                internal var label = ""
                private var currProgress = 0
                internal var maxValue = 100

                var progressView: View? = null
                private var progressBar: ProgressBar? = null
                private var progressLabel: TextView? = null

                fun createShallowClone(): CardProgress {
                    val progressClone = CardProgress()
                    progressClone.label = label
                    progressClone.currProgress = currProgress
                    progressClone.maxValue = maxValue
                    progressClone.progressType = progressType
                    return progressClone
                }

                /**
                 * Set the progress. Only useful for the type {@link #PROGRESS_TYPE_NORMAL}.
                 * @param progress
                 * @see android.widget.ProgressBar#setProgress(int)
                 */
                fun setProgress(progress: Int) {
                    currProgress = progress
                    val bar = progressBar
                    if (bar != null) {
                        bar.progress = currProgress
                        bar.invalidate()
                    }
                }

                /**
                 * Set the range of the progress to 0...max.
                 * Only useful for the type {@link #PROGRESS_TYPE_NORMAL}.
                 * @param max
                 * @see android.widget.ProgressBar#setMax(int)
                 */
                fun setMax(max: Int) {
                    maxValue = max
                    val bar = getProgressBar()
                    if (bar != null) {
                        bar.max = maxValue
                    }
                }

                /**
                 * Set the label text that appears near the progress indicator.
                 * @param text
                 */
                fun setProgressLabel(text: String) {
                    label = text
                    val labelView: TextView? = getProgressLabel()
                    if (labelView != null) {
                        labelView.text = text
                    }
                }

                /**
                 * Set how progress is displayed. The parameter must be one of three supported
                 * types:
                 * <ul>
                 *      <li>{@link Card#PROGRESS_TYPE_NORMAL: Standard progress bar with label text
                 *      </li>
                 *      <li>{@link Card#PROGRESS_TYPE_INDETERMINATE}: Indeterminate progress bar
                 *          with label txt</li>
                 *      <li>{@link Card#PROGRESS_TYPE_LABEL}: Label only, no progresss bar</li>
                 *</ul>
                 * @param type
                 */
                fun setProgressType(type: Int) {
                    progressType = type
                    if (progressView != null) {
                        when (type) {
                            PROGRESS_TYPE_NO_PROGRESS -> progressView!!.visibility = View.GONE
                            PROGRESS_TYPE_NORMAL -> {
                                progressView!!.visibility = View.VISIBLE
                                getProgressBar()?.isIndeterminate = false
                            }
                            PROGRESS_TYPE_INDETERMINATE -> {
                                progressView!!.visibility = View.VISIBLE
                                getProgressBar()?.isIndeterminate = false
                            }
                        }
                    }
                }

                private fun getProgressLabel(): TextView? {
                    return when {
                        progressLabel != null -> progressLabel
                        progressView != null -> {
                            progressLabel = progressView!!.findViewById(R.id.card_progress_text)
                                    as TextView?
                            progressLabel
                        }
                        else -> null
                    }
                }

                private fun getProgressBar(): ProgressBar? {
                    return when {
                        progressBar != null -> progressBar
                        progressView != null -> {
                            progressBar = progressView!!.findViewById(R.id.card_progress)
                                    as ProgressBar?
                            progressBar
                        }
                        else -> null
                    }
                }
            }
        }
    }

    private lateinit var mClickListener: OnCardClickListener

    // The card model contains a reference to its desired layout (for extensibility), title,
    // description, zero to many action buttons, and zero or 1 progress indicator
    private var mLayoutId = R.layout.card

    /**
     * Tag that uniquely identifies this card
     */
    private var mTag: String? = null

    private var mTitle: String? = null
    private var mDescription: String? = null

    private var mCardView: View? = null
    private var mOverlayView: View? = null
    private var mTitleView: TextView? = null
    private var mDescView: TextView? = null
    private var mActionAreaView: View? = null

    private var mOngoingAnimator: Animator? = null

    /**
     * Visual state, either {@link #CARD_STATE_NORMAL}, {@link #CARD_STATE_FOCUSED} or
     * {@link #CARD_STATE_INACTIVE}
     */
    private var mCardState = CARD_STATE_NORMAL

    /**
     * Represent actions that can be taken from the card. Stylistically, we can designate the action
     * as positive, negative (ok/cancel, for instance) or neutral.
     * This "type" can be used as a UI hint.
     */
    private val mCardActions = arrayListOf<Builder.CardAction>()

    /**
     * Some cards will have a sense of "progress" which should be associated with, but separated
     * from, its "parent" card. To push for simplicity, Cards are designed to have a maximum of one
     * progress indicator per Card
     */
    private var mCardProgress: Builder.CardProgress? = null

    fun getTag(): String? {
        return mTag
    }

    fun getView(): View? {
        return mCardView
    }

    fun setDescription(desc: String): Card {
        if (mDescView != null) {
            mDescription = desc
            mDescView!!.text = desc
        }
        return this
    }

    fun setTitle(title: String): Card {
        if (mTitleView != null) {
            mTitle = title
            mTitleView!!.text = title
        }
        return this
    }

    /**
     * Return the UI state, either {@link #CARD_STATE_NORMAL}, {@link #CARD_STATE_FOCUSED} or
     * {@link #CARD_STATE_INACTIVE}
     */
    fun getState(): Int {
        return mCardState
    }

    /**
     * Set the UI state.
     * Must be called from the UI Thread
     *
     * @param state, which is either {@link #CARD_STATE_NORMAL}, {@link #CARD_STATE_FOCUSED} or
     * {@link #CARD_STATE_INACTIVE}.
     * @return The card itseld, allowing for chaining of calls
     */
    @SuppressLint("ObjectAnimatorBinding")
    fun setState(state: Int): Card {
        mCardState = state
        if (mOverlayView != null) {
            if (mOngoingAnimator != null) {
                mOngoingAnimator!!.end()
                mOngoingAnimator = null
            }
            when (state) {
                CARD_STATE_NORMAL -> {
                    mOverlayView!!.visibility = View.GONE
                }
                CARD_STATE_FOCUSED -> {
                    mOverlayView!!.visibility = View.VISIBLE
                    mOverlayView!!.setBackgroundResource(R.drawable.card_overlay_focused)
                    // TODO check if this works, or replace it with the following:
                    // val animator = PropertyValuesHolder.ofFloat("alpha", 0f)
                    val animator = ObjectAnimator.ofFloat(mOverlayView,
                            "alpha",
                            0f)
                    animator.repeatMode = ObjectAnimator.REVERSE
                    animator.repeatCount = ObjectAnimator.INFINITE
                    animator.duration = 1000
                    animator.start()
                    mOngoingAnimator = animator
                }
                CARD_STATE_INACTIVE -> {
                    mOverlayView!!.visibility = View.VISIBLE
                    mOverlayView!!.alpha = 1f
                    mOverlayView!!.setBackgroundColor(Color.argb(0xaa, 0xcc, 0xcc,
                            0xcc))
                }
            }
        }
        return this
    }

    /**
     * Set the type of progress indicator.
     * The progress type can only be changed if the Card was initially build with a progress
     * indicator.
     * {@see Builder#setProgressType(int}
     *
     * @param progressType Must be a value of either {@link #PROGRESS_TYPE_NORMAL},
     * {@link #PROGRESS_TYPE_INDETERMINATE}, {@link #PROGRESS_TYPE_LABEL} or
     * {@link #PROGRESS_TYPE_NO_PROGRESS}.
     * @return The card itself, allows for chaining of calls
     */
    fun setProgressType(progressType: Int): Card {
        if (mCardProgress == null) {
            mCardProgress = Builder.CardProgress()
        }
        mCardProgress!!.setProgressType(progressType)
        return this
    }

    /**
     * Return the progress indicator type.
     *
     * @return A value of either {@link #PROGRESS_TYPE_NORMAL},
     * {@link #PROGRESS_TYPE_INDETERMINATE}, {@link #PROGRESS_TYPE_LABEL}. Otherwise if no progress
     * indicator is enabled, {@link #PROGRESS_TYPE_NO_PROGRESS} is returned.
     */
    fun getProgressType(): Int {
        if (mCardProgress == null) {
            return PROGRESS_TYPE_NO_PROGRESS
        }
        return mCardProgress!!.progressType
    }

    /**
     * Set the progress to the specified value. Only applicable if the card has a
     * {@link #PROGRESS_TYPE_NORMAL} progress type
     *
     * @param progress
     * @return The card itself, allows for chaining of calls
     * @see #setMaxProgress(int)
     */
    fun setProgress(progress: Int): Card {
        if (mCardProgress != null) {
            mCardProgress!!.setProgress(progress)
        }
        return this
    }

    /**
     * Sets the range of the progress to 0...max. Only applicable if the card has a
     * {@link #PROGRESS_TYPE_NORMAL} progress type.
     *
     * @return The card itself, allows for chaining of calls
     */
    fun setMaxProgress(max: Int): Card {
        if (mCardProgress != null) {
            mCardProgress!!.setMax(max)
        }
        return this
    }

    /**
     * Set the label text for the progress if the card has a progress type of
     * {@link #PROGRESS_TYPE_NORMAL}, {@link #PROGRESS_TYPE_INDETERMINATE} or
     * {@link #PROGRESS_TYPE_LABEL}
     * @param text Label text
     * @return The card itself, allows for chaining of calls
     */
    fun setProgressLabbel(text: String): Card {
        if (mCardProgress != null) {
            mCardProgress!!.setProgressLabel(text)
        }
        return this
    }

    /**
     * Toggle the visibility of the progress section of the card. Only applicable if
     * the card has a progress type of
     * {@link #PROGRESS_TYPE_NORMAL}, {@link #PROGRESS_TYPE_INDETERMINATE} or
     * {@link #PROGRESS_TYPE_LABEL}.
     *
     * @param isVisible
     * @return The card itself, allows for chaining of calls
     */
    fun setProgressVisibility(isVisible: Boolean): Card {
        if (mCardProgress?.progressView == null) {
            //Card does not have progress
            return this
        }
        mCardProgress!!.progressView!!.visibility = if (isVisible) {
            View.VISIBLE
        } else {
            View.GONE
        }
        return this
    }

    /**
     * Adds an aaction to this card during build time
     */
    private fun addAction(label: String, id: Int, type: Int) {
        val cardAction = Builder.CardAction()
        cardAction.label = label
        cardAction.id = id
        cardAction.type = type
        mCardActions.add(cardAction)
    }

    /**
     * Toggles the visibility of a card action
     */
    fun setActionVisibility(actionId: Int, isVisible: Boolean): Card {
        val visibilityFlag = if (isVisible) {
            View.VISIBLE
        } else {
            View.GONE
        }
        for (action in mCardActions) {
            if (action.id == actionId && action.actionView != null) {
                action.actionView!!.visibility = visibilityFlag
            }
        }
        return this
    }

    /**
     * Toggles visibility of the action area of the Card through an animation
     */
    fun setActionAreaVisibility(isVisible: Boolean): Card {
        if (mActionAreaView == null) {
            // Card does not have an action area
            return this
        }

        if (isVisible) {
            mActionAreaView!!.visibility = View.VISIBLE
            mActionAreaView!!.pivotX = (mCardView?.width ?: 0) / 2f
            mActionAreaView!!.pivotY = 0f
            mActionAreaView!!.alpha = .5f
            mActionAreaView!!.rotationX = -90f
            mActionAreaView!!.animate()
                    .rotationX(0f)
                    .alpha(1f)
                    .duration = ANIM_DURATION
        } else {
            mActionAreaView!!.pivotY = 0f
            mActionAreaView!!.pivotX = (mCardView?.width ?: 0) / 2f
            mActionAreaView!!.animate()
                    .rotationX(-90f)
                    .alpha(0f)
                    .setDuration(ANIM_DURATION)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            mActionAreaView!!.visibility = View.GONE
                        }
                    })
        }
        return this
    }

    /**
     * Creates a shallow clone of the card.  Shallow means all values are present, but no views.
     * This is useful for saving/restoring in the case of configuration changes, like screen
     * rotation.
     *
     * @return A shallow clone of the card instance
     */
    fun createShallowClone(): Card {
        val cloneCard = Card()

        // Outer card values
        cloneCard.mTitle = mTitle
        cloneCard.mDescription = mDescription
        cloneCard.mTag = mTag
        cloneCard.mLayoutId = mLayoutId
        cloneCard.mCardState = mCardState

        // Progress
        if (mCardProgress != null) {
            cloneCard.mCardProgress = mCardProgress!!.createShallowClone()
        }

        // Actions
        for (action in mCardActions) {
            cloneCard.mCardActions.add(action.createShallowClone())
        }
        return cloneCard
    }

    /**
     * Prepare the card to be stored for configuration change.
     */
    fun prepareForConfigurationChange() {
        // Null out views
        mCardView = null
        for (action in mCardActions) {
            action.actionView = null
        }
        mCardProgress?.progressView = null
    }
}