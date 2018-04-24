package com.crepetete.transittracker.intent.service.notification

import android.annotation.TargetApi
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

class GeofenceUpdateNotification(context: Context,
                                 private val mTitle: String,
                                 private val mText: String)
    : GeofenceNotification(context) {
    @TargetApi(Build.VERSION_CODES.O)
    override var mChannelId: String = CHANNEL_GEOFENCE_UPDATE

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getImportance(): Int {
        return NotificationManager.IMPORTANCE_HIGH
    }

    override fun getBuilder(notificationManager: NotificationManager): Builder {
        // Android O requires a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = mContext.getString(R.string.app_name)

            //Create a channel for the notification
            val channel = NotificationChannel(mChannelId, name, getImportance())
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

        // Define the notification settings
        return super.getBuilder(notificationManager)
                .setSmallIcon(mSmallIconId)
//                .setLargeIcon()
                .setColor(ContextCompat.getColor(mContext, R.color.colorPrimary))
                .setContentTitle(mTitle)
                .setContentText(mText)
                .setContentIntent(notificationPendingIntent)
                .setGroup(GROUP_GEOFENCES)
//                .addAction(android.R.drawable.ic_delete, "Remove", removePendingIntent)
                // Dismiss notification once the user touches it
                .setAutoCancel(true)
    }

    override fun getSpecificTitle(): String? {
        return null
    }

}