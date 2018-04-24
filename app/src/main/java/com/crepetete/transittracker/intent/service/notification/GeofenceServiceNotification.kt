package com.crepetete.transittracker.intent.service.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.TaskStackBuilder
import android.support.v4.content.ContextCompat
import com.crepetete.transittracker.R
import com.crepetete.transittracker.intent.service.notification.`super`.GeofenceNotification
import com.crepetete.transittracker.views.activities.main.MainActivity

class GeofenceServiceNotification(context: Context) : GeofenceNotification(context) {
    override var mChannelId = CHANNEL_GEOFENCE_SERVICE

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
            channel.description = "Notifications when the user enters or leaves an area."

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

        // Define the notification settings
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(mContext, mChannelId).setChannelId(mChannelId)
        } else {
            Notification.Builder(mContext)
        }

        builder.setSmallIcon(R.drawable.ic_notif_transit)
//                .setLargeIcon()
                .setColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                .setContentTitle("Geofences are running in foreground")
                .setContentText(mContext.getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent)
                .setGroup(GROUP_GEOFENCES)
//                .addAction(android.R.drawable.ic_delete, "Remove", removePendingIntent)

        // Dismiss notification once the user touches it
        builder.setAutoCancel(true)

//        val n = NotificationExtras.buildWithBackgroundColor(this, builder, -0x10000)

        return builder
    }
}