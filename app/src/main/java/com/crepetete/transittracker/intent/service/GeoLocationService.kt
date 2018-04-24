package com.crepetete.transittracker.intent.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.IBinder
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import timber.log.Timber

class GeoLocationService : Service(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {
    /**
     * Called when the provider status changes. This method is called when
     * a provider is unable to fetch a location or if the provider has recently
     * become available after a period of unavailability.
     *
     * @param provider the name of the location provider associated with this
     * update.
     * @param status [LocationProvider.OUT_OF_SERVICE] if the
     * provider is out of service, and this is not expected to change in the
     * near future; [LocationProvider.TEMPORARILY_UNAVAILABLE] if
     * the provider is temporarily unavailable but is expected to be available
     * shortly; and [LocationProvider.AVAILABLE] if the
     * provider is currently available.
     * @param extras an optional Bundle which will contain provider specific
     * status variables.
     *
     *
     *  A number of common key/value pairs for the extras Bundle are listed
     * below. Providers that use any of the keys on this list must
     * provide the corresponding value as described below.
     *
     *
     *  *  satellites - the number of satellites used to derive the fix
     *
     */
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this
     * update.
     */
    override fun onProviderEnabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onResult(p0: Status) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
//        const val GEOLOCATION_SERVICE_INTENT = "GEOLOCATION_SERICE_INTENT"

        private const val UPDATE_INTERVAL_IN_MILLISECONDS = 10000L
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
                UPDATE_INTERVAL_IN_MILLISECONDS / 2
    }

    private val mFusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    private var mGoogleApiClient: GoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()

    private val mLocationRequest: LocationRequest by lazy {
        LocationRequest().setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

//    private val mPendingIntent: PendingIntent by lazy {
//
//    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mGoogleApiClient.connect()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mGoogleApiClient.isConnected) {
            mGoogleApiClient.disconnect()
        }
    }

    fun broadcastLocationFound(location: Location) {
        TODO("re-implement")
//        val intent = Intent(GEOLOCATION_SERVICE_INTENT)
//        intent.putExtra("latitude", location.latitude)
//        intent.putExtra("longitude", location.longitude)
//        intent.putExtra("done", 1)
//
//        sendBroadcast(intent)
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, LocationCallback(), null)
    }

    fun stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(LocationCallback())
    }

    override fun onConnected(p0: Bundle?) {
        startLocationUpdates()
    }

    override fun onConnectionSuspended(cause: Int) {
        mGoogleApiClient.connect()
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Timber.d("Connection failed: Errorcode: ${result.errorCode}")
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            broadcastLocationFound(location)

            if (location.accuracy <= 50) {
                stopSelf()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}