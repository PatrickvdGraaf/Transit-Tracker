package com.crepetete.transittracker.intent.broadcast

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.text.TextUtils
import com.crepetete.transittracker.R
import com.crepetete.transittracker.config.GeofenceErrorMessages
import com.crepetete.transittracker.intent.service.notification.GeofenceUpdateNotification
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import timber.log.Timber


class GeofenceBroadCastReceiver : BroadcastReceiver() {
    companion object {
        internal const val GEOFENCE_ACTION_CHANNEL_ID = "GEOFENCE_ACTION_CHANNEL_ID"
        const val GEOFENCE_ACTION = "com.example.geofence.ACTION_RECEIVE_GEOFENCE"
    }

    private val mBroadCastIntent = Intent()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            if (geofencingEvent.hasError()) {
                handleError(context, geofencingEvent)
            } else {
                handleEnterExit(context, geofencingEvent)
            }
        }
    }

    private fun handleError(context: Context, geofencingEvent: GeofencingEvent) {
        // Get error code
        val errorMessage = GeofenceErrorMessages.getErrorString(context,
                geofencingEvent.errorCode)

        // Log the error
        Timber.e("Geofence handleEerror: $errorMessage")

        // Set the action and error message for the broadcast intent
        mBroadCastIntent.setAction(GEOFENCE_ACTION).putExtra("GEOFENCE_STATUS", errorMessage)

        // Broadcast the error locally to other components in the app
        LocalBroadcastManager.getInstance(context).sendBroadcast(mBroadCastIntent)
    }

    private fun handleEnterExit(context: Context, geofencingEvent: GeofencingEvent) {
        // Get the type of transition
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that a valid transition was reported
        if ((geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
                || (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            sendNotification(context, getGeofenceTransitionDetails(context, geofenceTransition,
                    triggeringGeofences))

            for (fence in triggeringGeofences) {
                // Create an Intent to broadcast to the app.
                mBroadCastIntent.setAction(GEOFENCE_ACTION)
                        .putExtra("EXTRA_GEOFENCE_ID", fence.requestId)
                        .putExtra("EXTRA_GEOFENCE_TRANSITION_TYPE", geofenceTransition)
                LocalBroadcastManager.getInstance(context).sendBroadcast(mBroadCastIntent)
            }
        } else {
            Timber.e("Geofence transition error: invalid transition type $geofenceTransition")
        }
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
//    private fun sendNotification(context: Context, notificationDetails: String) {
//        val text = context.getString(R.string.geofence_transition_notification_text)
//        // Get an instance of the Notification manager
//        val notificationManager: NotificationManager =
//                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        // Issue the notification
//        notificationManager.notify(0,
//                GeofenceUpdateNotification(context,
//                notificationDetails,
//                text).getBuilder().build())
//    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private fun sendNotification(context: Context, notificationDetails: String) {
        // Get an instance of the Notification manager
        val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Issue the notification))
        notificationManager.notify(0, GeofenceUpdateNotification(context,
                notificationDetails,
                context.getString(R.string.geofence_transition_notification_text)).getBuilder(notificationManager).build())
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private fun getTransitionString(context: Context, transitionType: Int): String {
        return when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER ->
                context.getString(R.string.geofence_transition_entered)
            Geofence.GEOFENCE_TRANSITION_EXIT ->
                context.getString(R.string.geofence_transition_exited)
            else -> context.getString(R.string.unknown_geofence_transition)

        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private fun getGeofenceTransitionDetails(context: Context, geofenceTransition: Int,
                                             triggeringGeofences: List<Geofence>): String {
        val geofenceTransitionString = getTransitionString(context, geofenceTransition)

        // Get the IDs of each geofence that was triggered
        val triggeringGeofencesIdsList = arrayListOf<String>()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.requestId)
        }
        val triggeringGeofencesIdsString = TextUtils.join(", ",
                triggeringGeofencesIdsList)

        return "$geofenceTransitionString : $triggeringGeofencesIdsString"
    }
}