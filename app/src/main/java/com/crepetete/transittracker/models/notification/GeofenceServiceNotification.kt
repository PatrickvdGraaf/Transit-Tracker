package com.crepetete.transittracker.models.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.TaskStackBuilder
import android.support.v4.content.ContextCompat
import com.crepetete.transittracker.R
import com.crepetete.transittracker.models.intent.broadcast.GeofenceBroadCastReceiver
import com.crepetete.transittracker.models.notification.base.GeofenceNotification
import com.crepetete.transittracker.views.activities.main.MainActivity

class GeofenceServiceNotification(context: Context) : GeofenceNotification(context) {
//    override var mChannelId = CHANNEL_GEOFENCE_SERVICE
    override var mChannelId = "CHANNEL_ID"

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getImportance(): Int {
        return NotificationManager.IMPORTANCE_HIGH
    }

    override fun getSpecificTitle(): String? {
        return null
    }

    @SuppressLint("MissingSuperCall")
    override fun getBuilder(notificationManager: NotificationManager): Builder {
//        return super.getBuilder()
//                .setSmallIcon(mSmallIconId)
//                .setColor(ContextCompat.getColor(mContext, R.color.colorAccent))
//                .setContentTitle("Geofences are running in foreground")
//                .setContentText(mContext.getString(R.string.geofence_transition_notification_text))
//                .setContentIntent(mNotificationPendingIntent)
//                // Dismiss notification once the user touches it
//                .setAutoCancel(true)
////                .setLargeIcon()
////                .addAction(android.R.drawable.ic_delete, "Remove", removePendingIntent)
        // Android O requires a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = mContext.getString(R.string.app_name)

            //Create a channel for the notification
            val channel = NotificationChannel(mChannelId, name,
                    NotificationManager.IMPORTANCE_HIGH)
            channel.description = getChannelDescription()

            // Set the Notification Channel for the Notification Manager
            notificationManager.createNotificationChannel(channel)
        }

        // Create an explicit content Intent that starts with the main Activity
        val notificationIntent = Intent(mContext, MainActivity::class.java)

        // Construct a task stack
        val stackBuilder = TaskStackBuilder.create(mContext)

        // Add the main Activity to the task stack as the parent
        stackBuilder.addParentStack(MainActivity::class.java)

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent)

        // Get a PendingIntent containing the entire back stack
        val notificationPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT)
//
//        val removeIntent = Intent(context, GeofenceBroadCastReceiver::class.java)
//        removeIntent.action = GeofenceBroadCastReceiver.ACTION_REMOVE
//        removeIntent.putExtra(GeofenceBroadCastReceiver.VALUE_LIST, triggeringGeofences.toTypedArray())
//        val removePendingIntent = PendingIntent.getBroadcast(context, 0,
//                removeIntent, 0)

        val stopAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Notification.Action.Builder(Icon.createWithResource(mContext, R.drawable.ic_stop_24dp), "Stop",
                    GeofenceBroadCastReceiver.getStopIntent(mContext)).build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Action.Builder(R.drawable.ic_stop_24dp, "Stop",
                    GeofenceBroadCastReceiver.getStopIntent(mContext)).build()
        }

        // Define the notification settings
        return super.getBuilder(notificationManager)
                .setSmallIcon(R.drawable.ic_notif_transit)
//                .setLargeIcon()
                .setColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark))
                .setContentTitle("Geofences are running in foreground")
                .setContentText(mContext.getString(R.string.geofence_transition_notification_text))
//                .setContentIntent(notificationPendingIntent) Open App on Click
                .setGroup(GROUP_GEOFENCES)
                // Dismiss notification once the user touches it
                .setAutoCancel(true)
                .addAction(stopAction)
    }
}