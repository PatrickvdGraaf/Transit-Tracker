package com.crepetete.transittracker.models.intent.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.IBinder
import com.crepetete.transittracker.models.intent.broadcast.GeofenceBroadCastReceiver
import com.crepetete.transittracker.models.notification.GeofenceServiceNotification
import com.crepetete.transittracker.models.notification.base.GeofenceNotification
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import timber.log.Timber

class GeofenceService : Service(), GoogleApiClient.ConnectionCallbacks, LocationListener,
        GoogleApiClient.OnConnectionFailedListener {
    companion object {
        private const val GEOFENCE_SERVICE_ID = 2

        private const val UPDATE_INTERVAL_IN_MILLISECONDS = 10000L
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
                UPDATE_INTERVAL_IN_MILLISECONDS / 2
    }

    private var mReceiver: GeofenceBroadCastReceiver? = null

    private val mFusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    private val mGoogleApiClient: GoogleApiClient by lazy {
        GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build()
    }

    private val mLocationRequest: LocationRequest by lazy {
        LocationRequest().setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(GEOFENCE_SERVICE_ID, getServiceNotification(this))
        registerGeofenceReceiver()
    }


    private fun getServiceNotification(context: Context): Notification {

        // Get an instance of the Notification manager
        val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        return GeofenceServiceNotification(context).getBuilder(notificationManager).build()
        // Android O requires a notification channel
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = context.getString(R.string.app_name)
//
//            //Create a channel for the notification
//            val channel = NotificationChannel(GeofenceNotification.CHANNEL_GEOFENCE_SERVICE, name,
//                    NotificationManager.IMPORTANCE_HIGH)
//            channel.description = "Notifications when the user enters or leaves an area."
//
//            // Set the Notification Channel for the Notification Manager
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        // Create an explicit content Intent that starts with the main Activity
//        val notificationIntent = Intent(context, MainActivity::class.java)
//
//        // Construct a task stack
//        val stackBuilder = TaskStackBuilder.create(context)
//
//        // Add the main Activity to the task stack as the parent
//        stackBuilder.addParentStack(MainActivity::class.java)
//
//        // Push the content Intent onto the stack.
//        stackBuilder.addNextIntent(notificationIntent)
//
//        // Get a PendingIntent containing the entire back stack
//        val notificationPendingIntent = stackBuilder.getPendingIntent(0,
//                PendingIntent.FLAG_UPDATE_CURRENT)
////
////        val removeIntent = Intent(context, GeofenceBroadCastReceiver::class.java)
////        removeIntent.action = GeofenceBroadCastReceiver.ACTION_REMOVE
////        removeIntent.putExtra(GeofenceBroadCastReceiver.VALUE_LIST, triggeringGeofences.toTypedArray())
////        val removePendingIntent = PendingIntent.getBroadcast(context, 0,
////                removeIntent, 0)
//
//        // Define the notification settings
//        val builder = NotificationCompat.Builder(context,
//                GeofenceNotification.CHANNEL_GEOFENCE_UPDATE)
//        builder.setSmallIcon(R.drawable.ic_notif_transit)
////                .setLargeIcon()
//                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
//                .setContentTitle("Geofences are running in foreground")
//                .setContentText(context.getString(R.string.geofence_transition_notification_text))
//                .setContentIntent(notificationPendingIntent)
////                .addAction(android.R.drawable.ic_delete, "Remove", removePendingIntent)
//
//        // Set the Channel ID for Android O.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            builder.setChannelId(GeofenceNotification.CHANNEL_GEOFENCE_SERVICE)
//        }
//
//        // Dismiss notification once the user touches it
//        builder.setAutoCancel(true)
//
////        val n = NotificationExtras.buildWithBackgroundColor(this, builder, -0x10000)
//
//        return builder.build()
    }


    private fun registerGeofenceReceiver() {
        mReceiver = GeofenceBroadCastReceiver()
        registerReceiver(mReceiver, IntentFilter(GeofenceBroadCastReceiver.GEOFENCE_ACTION))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mGoogleApiClient.connect()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onConnected(p0: Bundle?) {
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, LocationCallback(), null)
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            val intent = Intent(GeofenceNotification.CHANNEL_GEOFENCE_SERVICE)
            intent.putExtra("latitude", location.latitude)
            intent.putExtra("longitude", location.longitude)
            intent.putExtra("done", 1)

            sendBroadcast(intent)

            if (location.accuracy <= 50) {
                stopSelf()
            }
        }
    }

    private fun stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(LocationCallback())
    }

    override fun onConnectionSuspended(p0: Int) {
        mGoogleApiClient.connect()
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Timber.d("Connection failed: Errorcode: ${result.errorCode}")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Timber.i("onStatusChanged: $status")
    }

    /**
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this
     * update.
     */
    override fun onProviderEnabled(provider: String?) {
        startLocationUpdates()
    }

    /**
     * Called when the provider is disabled by the user. If requestLocationUpdates
     * is called on an already disabled provider, this method is called
     * immediately.
     *
     * @param provider the name of the location provider associated with this
     * update.
     */
    override fun onProviderDisabled(provider: String?) {
        stopLocationUpdates()
    }

    override fun onDestroy() {
        stopLocationUpdates()
        if (mGoogleApiClient.isConnected) {
            mGoogleApiClient.disconnect()
        }

        unregisterReceiver(mReceiver)
        mReceiver = null
        super.onDestroy()
    }
}