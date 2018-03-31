package com.crepetete.transittracker.intent

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.JobIntentService
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import com.crepetete.transittracker.R
import com.crepetete.transittracker.activities.main.MainActivity
import com.crepetete.transittracker.config.GeofenceErrorMessages
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import timber.log.Timber

/**
 * Listener for geofence transition changes.
 *
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a notification
 * as the output.
 */
class GeofenceTransitionsJobIntentService : JobIntentService() {
    companion object {
        private const val mJobID = 573

        /**
         * Convenience method for enqueuing work in to this service.
         */
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, GeofenceTransitionsJobIntentService::class.java, mJobID, intent)
        }
    }

    private val mChannelID = "channel_01"

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    override fun onHandleWork(intent: Intent) {
        val geofenceEvent = GeofencingEvent.fromIntent(intent)
        if (geofenceEvent.hasError()) {
            Timber.e(GeofenceErrorMessages.getErrorString(this, geofenceEvent.errorCode))
            return
        }

        // Get the transition type.
        val geofenceTransition = geofenceEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            val triggeringGeofences = geofenceEvent.triggeringGeofences

            // Get the transition details as a String
            val geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,
                    triggeringGeofences)

            // Send notification and log the transition details
            sendNotification(geofenceTransitionDetails)
            Timber.i(geofenceTransitionDetails)
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private fun getGeofenceTransitionDetails(geofenceTransition: Int,
                                             triggeringGeofences: List<Geofence>): String {
        val geofenceTransitionString = getTransitionString(geofenceTransition)

        // Get the IDs of each geofence that was triggered
        val triggeringGeofencesIdsList = arrayListOf<String>()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.requestId)
        }
        val triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList)

        return " $geofenceTransitionString : $triggeringGeofencesIdsString"
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private fun sendNotification(notificationDetails: String) {
        // Get an instance of the Notification manager
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)

            //Create a channel for the notification
            val channel = NotificationChannel(mChannelID, name,
                    NotificationManager.IMPORTANCE_HIGH)

            // Set the Notification Channel for the Notification Manager
            notificationManager.createNotificationChannel(channel)
        }

        // Create an explicit content Intent that starts with the main Activity
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)

        // Construct a task stack
        val stackBuilder = TaskStackBuilder.create(this)

        // Add the main Activity to the task stack as the parent
        stackBuilder.addParentStack(MainActivity::class.java)

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent)

        // Get a PendingIntent containing the entire back stack
        val notificationPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT)

        // Define the notification settings
        // TODO create real notification icons
        val builder = NotificationCompat.Builder(this, mChannelID)
        builder.setSmallIcon(R.mipmap.ic_launcher)
//                .setLargeIcon()
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                .setContentTitle(notificationDetails)
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent)

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(mChannelID)
        }

        // Dismiss notification once the user touches it
        builder.setAutoCancel(true)

        // Issue the notification
        notificationManager.notify(0, builder.build())
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private fun getTransitionString(transitionType: Int): String {
        return when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER ->
                getString(R.string.geofence_transition_entered)
            Geofence.GEOFENCE_TRANSITION_EXIT ->
                getString(R.string.geofence_transition_exited)
            else -> getString(R.string.unknown_geofence_transition)

        }
    }
}