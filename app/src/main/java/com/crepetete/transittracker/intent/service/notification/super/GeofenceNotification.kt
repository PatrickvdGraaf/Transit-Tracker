package com.crepetete.transittracker.intent.service.notification.`super`

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.CallSuper
import android.support.annotation.DrawableRes
import android.support.annotation.IntRange
import android.support.annotation.RequiresApi
import com.crepetete.transittracker.R
import com.crepetete.transittracker.views.activities.main.MainActivity
import kotlin.reflect.KClass

abstract class GeofenceNotification : Notification {
    companion object {
        const val CHANNEL_GEOFENCE_UPDATE = "CHANNEL_GEOFENCE_UPDATE"
        const val CHANNEL_GEOFENCE_SERVICE = "CHANNEL_GEOFENCE_SERVICE"

        const val GROUP_GEOFENCES = "GROUP_GEOFENCES"
    }

    internal var mContext: Context

    internal val mNotificationPendingIntent: PendingIntent

    @DrawableRes
    internal val mSmallIconId = R.drawable.ic_notif_transit

    internal abstract var mChannelId: String

    constructor(context: Context) : this(context, null)

    private constructor(context: Context, detailActivity: KClass<Any>?) {
        mContext = context

        // Construct a task stack
        val stackBuilder = TaskStackBuilder.create(context)

        // Add the main Activity to the task stack as the parent
        stackBuilder.addParentStack(MainActivity::class.java)

//        if (detailActivity != null) {
        // Create an explicit content Intent that starts with the main Activity
        val notificationIntent = Intent(context, MainActivity::class.java)

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent)
//        }

        // Get a PendingIntent containing the entire back stack
        mNotificationPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @IntRange(from = NotificationManager.IMPORTANCE_NONE.toLong(),
            to = NotificationManager.IMPORTANCE_MAX.toLong())
    abstract fun getImportance(): Int

    @CallSuper
    open fun getBuilder(notificationManager: NotificationManager): Builder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Builder(mContext, mChannelId).setChannelId(mChannelId)
        } else {
            Builder(mContext)
        }
    }

    abstract fun getSpecificTitle(): String?

    @TargetApi(Build.VERSION_CODES.O)
    internal fun getChannelDescription(): String {
        return when (mChannelId) {
            CHANNEL_GEOFENCE_UPDATE -> {
                "Notifications when entering or exiting a marked area."
            }
            CHANNEL_GEOFENCE_SERVICE -> {
                "Notification that shows when the app is still waiting for marked locations."
            }
            else -> {
                "Other Notifications"
            }
        }
    }
}