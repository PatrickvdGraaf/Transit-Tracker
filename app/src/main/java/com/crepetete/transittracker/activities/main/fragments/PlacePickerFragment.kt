package com.crepetete.transittracker.activities.main.fragments

import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment
import android.widget.Toast
import com.crepetete.transittracker.R
import com.crepetete.transittracker.activities.main.fragments.map.cardstream.Card
import com.crepetete.transittracker.activities.main.fragments.map.cardstream.CardStream
import com.crepetete.transittracker.activities.main.fragments.map.cardstream.CardStreamFragment
import com.crepetete.transittracker.activities.main.fragments.map.cardstream.OnCardClickListener
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlacePicker
import timber.log.Timber

/**
 * Sample demonstrating the use of {@link PlacePicker}.
 * This sample shows the construction of an {@link Intent} to open the PlacePicker from the
 * Google Places API for Android and select a {@link Place}.
 *
 * This sample uses the CardStream sample template to create the UI for this demo, which is not
 * required to use the PlacePicker API. (Please see the Readme-CardStream.txt file for details.)
 */
class PlacePickerFragment : OnCardClickListener, Fragment() {
    companion object {
        private const val CARD_INTRO = "INTRO"
        private const val CARD_PICKER = "PICKER"
        private const val CARD_DETAIL = "DETAIL"

        /**
         * Action to launch the PlacePicker from a card. Identifies the card action.
         */
        private const val ACTION_PICK_PLACE = 1

        /**
         * Request code passed to the PlacePicker intent to identify its result when it returns.
         */
        private const val REQUEST_PLACE_PICKER = 1
    }

    private var mCards: CardStreamFragment? = null

    // Buffer used to display a list of place types for a place
    private val mPlaceTypeDisplayBuffer = StringBuffer()

    override fun onResume() {
        super.onResume()

        // Check if cards are visible, at least the picker card is always shown.
        val stream: CardStreamFragment = getCardStream()
        if (stream.getVisibleCardCount() < 1) {
            // No cards are visible, fragment is started for the first time.
            // Prepare all cards and show the intro card.
            initialiseCards()

            // Show the picker card and make it non-dismissible
            getCardStream().showCard(CARD_PICKER, false)
        }
    }

    override fun onCardClick(cardActionId: Int, cardTag: String) {
        if (cardActionId == ACTION_PICK_PLACE) {
            try {
                val intentBuilder = PlacePicker.IntentBuilder()
                val intent = intentBuilder.build(activity)
                // Start the Intent by requesting a result, identified by a request code.
                startActivityForResult(intent, REQUEST_PLACE_PICKER)

                // Hide the pick option in the UI to prevent users from starting the picker multiple
                // times.
                showPickAction(false)
            } catch (e: GooglePlayServicesRepairableException) {
                GoogleApiAvailability.getInstance().getErrorDialog(activity,
                        e.connectionStatusCode, 0)
            } catch (e: GooglePlayServicesNotAvailableException) {
                Toast.makeText(activity, "Google Play Services is not available",
                        Toast.LENGTH_SHORT).show()
            }
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

            // Enable the picker option
            showPickAction(true)

            if (resultCode == Activity.RESULT_OK) {
                /* User has picked a place, extract data.
                Data is extracted from the returned intent by retrieving a Place object from the
                PlacePicker.
                 */
                val place = PlacePicker.getPlace(activity, data)

                /* A Place object contains details about the place, such as its name, address and
                phone number. Extract the name, address, phone number, place ID and place types.
                 */
                val name = place.name
                val address = place.address
                val phone = place.phoneNumber
                val placeId = place.id
                var attribution: CharSequence? = place.attributions
                if (attribution == null) {
                    attribution = ""
                }
                val latLng = place.latLng

                // Update data on card
                getCardStream().getCard(CARD_DETAIL)
                        ?.setTitle(name.toString())
                        ?.setDescription(getString(R.string.detail_text, placeId, address, phone,
                                attribution))

                // Print data to debug
                Timber.d("Place selected: $placeId ($name)")

                // Show the card
                getCardStream().showCard(CARD_DETAIL)
            } else {
                // User has not selected a place, hide the card
                getCardStream().hideCard(CARD_DETAIL)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Initializes the picker and detail cards and adds them to the card stream.
     */
    private fun initialiseCards() {
        val activity = activity ?: return

        // Add a picker card.
        var c: Card = Card.Companion.Builder(this, CARD_PICKER)
                .setTitle(getString(R.string.pick_title))
                .setDescription(getString(R.string.pick_text))
                .addAction(getString(R.string.pick_action), ACTION_PICK_PLACE, Card.ACTION_NEUTRAL)
                .setLayout(R.layout.card_google)
                .build(activity)

        getCardStream().addCard(c, false)

        // Add a detail card
        c = Card.Companion.Builder(this, CARD_DETAIL)
                .setTitle(getString(R.string.empty))
                .setDescription(getString(R.string.empty))
                .build(activity)
        getCardStream().addCard(c, false)

        // Add and show introduction card.
        c = Card.Companion.Builder(this, CARD_INTRO)
                .setTitle(getString(R.string.intro_title))
                .setDescription(getString(R.string.intro_message))
                .build(activity)
        getCardStream().addCard(c, true)
    }

    /**
     * Sets the visibility of the 'Pick Action' option on the 'Pick a place' card.
     * The action should be hidden when the PlacePicker Intent has been fired to prevent it from
     * being launched multiple times simultaneously.
     * @param show
     */
    private fun showPickAction(show: Boolean) {
        mCards?.getCard(CARD_PICKER)?.setActionVisibility(ACTION_PICK_PLACE, show)
    }

    /**
     * Returns the CardStream.
     * @return
     */
    private fun getCardStream(): CardStreamFragment {
        if (mCards == null) {
            if (activity is CardStream) {
                mCards = (activity as CardStream).getCardStream()
            } else {
                throw ClassCastException("Parent activity of PlacePickerFragment should implement" +
                        "the CardStream interface.")
            }
        }
        return mCards!!
    }
}