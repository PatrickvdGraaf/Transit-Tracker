package com.crepetete.transittracker.activities.main.fragments.map.cardstream

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.crepetete.transittracker.R

/**
 * A Fragment that handles a stream of cards.
 * Cards can be shown or hidden. When a card is shown it can also be marked as not-dismissible, see
 * {@link CardStreamLinearLayout#addCard(android.view.View, boolean)}
 */
class CardStreamFragment : Fragment() {
    private val mInitialSize = 15
    private var mLayout: CardStreamLinearLayout? = null
    private val mVisibleCards: LinkedHashMap<String, Card> = LinkedHashMap(mInitialSize)
    private val mHiddenCards: HashMap<String?, Card> = HashMap(mInitialSize)
    private val mDismissibleCards: HashSet<String> = HashSet(mInitialSize)

    // Set the listener to handle dismissed cards by moving them to the hidden cards map.
    private val mCardDismissListener: CardStreamLinearLayout.OnDismissListener =
            object : CardStreamLinearLayout.OnDismissListener {
                override fun onDismiss(tag: String) {
                    dismissCard(tag)
                }
            }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.card_stream, container, false)
        mLayout = view.findViewById(R.id.card_stream) as CardStreamLinearLayout
        mLayout?.setOnDismissListener(mCardDismissListener)

        return view
    }

    /**
     * Add a visible, dismissible card to the card stream.
     *
     * @param card
     */
    fun addCard(card: Card) {
        val tag = card.getTag()

        if (!mVisibleCards.containsKey(tag) && !mHiddenCards.containsKey(tag)) {
            val view = card.getView()
            view?.tag = tag
            mHiddenCards[tag] = card
        }
    }

    /**
     * Add and show a card
     *
     * @param card
     * @param show
     */
    fun addCard(card: Card, show: Boolean) {
        addCard(card)
        if (show) {
            showCard(card.getTag().toString())
        }
    }

    /**
     * Remove a card.
     *
     * @param tag Tag of the card that needs to be removed.
     * @return Boolean representing whether the card has been successfully removed.
     */
    fun removeCard(tag: String): Boolean {
        //Attempt to remove a visible card first
        var card: Card? = mVisibleCards[tag]
        return if (card != null) {
            // Card is visible, also remove it from layout
            mVisibleCards.remove(tag)
            mLayout?.removeView(card.getView())
            true
        } else {
            // Card is hidden, no need to remove from layout/
            card = mHiddenCards.remove(tag)
            card != null
        }
    }

    /**
     * Show a dismissible card.
     *
     * @return whether the card could be shown or not.
     */
    fun showCard(tag: String): Boolean {
        return showCard(tag, true)
    }

    /**
     * Show a card.
     *
     * @param tag
     * @param dismissible
     * @return whether the card could be shown or not
     */
    fun showCard(tag: String, dismissible: Boolean): Boolean {
        val card: Card? = mHiddenCards[tag]
        
        // Ensure the card is hidden and not already visible
        if (card != null && !mVisibleCards.contains(tag)) {
            mHiddenCards.remove(tag)
            mVisibleCards[tag] = card
            mLayout?.addCard(card.getView()!!, dismissible)
            if (dismissible) {
                mDismissibleCards.add(tag)
            }
            return true
        }
        return false
    }

    /**
     * Hides the card
     *
     * @param tag Identifier of the card that needs to be hidden
     * @return whether the card could be hidden or not
     */
    fun hideCard(tag: String): Boolean {
        val card: Card? = mVisibleCards[tag]
        if (card != null) {
            mVisibleCards.remove(tag)
            mDismissibleCards.remove(tag)
            mHiddenCards[tag] = card

            mLayout?.removeView(card.getView())
            return true
        }
        return mHiddenCards.contains(tag)
    }

    private fun dismissCard(tag: String) {
        val card: Card? = mVisibleCards[tag]
        if (card != null) {
            mDismissibleCards.remove(tag)
            mVisibleCards.remove(tag)
            mHiddenCards[tag] = card
        }
    }

    /**
     * @param tag of the card in question
     * @return true if the card is visible
     */
    fun isCardVisible(tag: String): Boolean {
        return mVisibleCards.contains(tag)
    }

    /**
     * @param tag of the card in question
     * @return true if the card is dismissible
     */
    fun isCardDismissible(tag: String): Boolean {
        return mDismissibleCards.contains(tag)
    }

    /**
     * @param tag of the card in question
     * @return Card object for the given tag, if it's inside either the mVisibleCards or
     * mHiddenCards array
     */
    fun getCard(tag: String): Card? {
        val card: Card? = mVisibleCards[tag]
        return card ?: mHiddenCards[tag]
    }

    /**
     * Moves the view port to show te card with this tag.
     *
     * @param tag of the Card in question
     * @see CardStreamLinearLayout#setFirstVisibleCard(String)
     */
    fun setFirstVisibleCard(tag: String) {
        val card: Card? = mVisibleCards[tag]
        if (card != null) {
            mLayout?.setFirstVisibleCard(tag)
        }
    }

    fun getVisibleCardCount(): Int {
        return mVisibleCards.size
    }

    fun getVisibleCards(): Collection<Card> {
        return mVisibleCards.values
    }

    fun restoreState(state: CardStreamState?, callback: OnCardClickListener) {
        // Restore hidden cards
        for (c in state?.hiddenCards!!) {
            val card = Card.Companion.Builder(callback, c).build(activity as FragmentActivity)
            mHiddenCards[card.getTag()] = card
        }

        // Temporarily set up list of dismissible cards
        val dismissibleCards = state.dismissibleCards

        // Restore shown cards
        for (c in state.visibleCards) {
            val card = Card.Companion.Builder(callback, c).build(activity as FragmentActivity)
            addCard(card)
            val tag = card.getTag()
            showCard(tag.toString(), dismissibleCards.contains(tag))
        }

        // Move to first visible card
        val firstShown = state.shownTag
        if (firstShown != null) {
            mLayout?.setFirstVisibleCard(firstShown)
        }

        mLayout?.triggerShowInitialAnimation()
    }

    fun dumpState(): CardStreamState {
        val visible = cloneCards(mVisibleCards.values)
        val hidden = cloneCards(mHiddenCards.values)
        val dismissible = HashSet<String>(mDismissibleCards)
        val firstVisible = mLayout?.getFirstVisibleCardTag()

        return CardStreamState(visible, hidden, dismissible, firstVisible)
    }

    private fun cloneCards(cards: Collection<Card>): Array<Card> {
        val cardArray = arrayListOf<Card>()
        for (c in cards) {
            cardArray.add(c.createShallowClone())
        }

        return cardArray.toTypedArray()
    }
}