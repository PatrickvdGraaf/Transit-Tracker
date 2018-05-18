package com.crepetete.transittracker.models.place

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.crepetete.transittracker.config.locale.LocaleHelper
import com.crepetete.transittracker.models.database.DatabaseBitmapUtility
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLng
import java.util.*


class ParcelablePlace @SuppressLint("ValidFragment") private constructor(
        internal val id: String,
        internal val name: String,
        internal val address: String,
        internal val latLng: LatLng,
        private val locale: Locale,
        private val website: Uri?,
        private val attributions: String,
        private var mImage: Bitmap?) : Parcelable {

    companion object CREATOR : Parcelable.Creator<ParcelablePlace> {
        // SQLite
        const val TABLE_NAME = "places"

        const val COLUMN_ID = "id"
        const val COLUMN_PLACE_ID = "place_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_ADDRESS = "address"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_WEBSITE = "website"
        const val COLUMN_ATTRIBUTIONS = "attributions:"
        const val COLUMN_IMAGE = "image"

        const val CREATE_TABLE = "CREATE TABLE $TABLE_NAME(" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_ADDRESS TEXT," +
                "$COLUMN_NAME TEXT)" +
                "$COLUMN_LATITUDE DOUBLE)" +
                "$COLUMN_LONGITUDE DOUBLE)" +
                "$COLUMN_WEBSITE TEXT)" +
                "$COLUMN_ATTRIBUTIONS TEXT)" +
                "$COLUMN_IMAGE BLOB)"

        fun getCursor(database: SQLiteDatabase, id: Long): Cursor? {
            val cursor = database.query(
                    ParcelablePlace.TABLE_NAME,
                    arrayOf(COLUMN_ID, COLUMN_PLACE_ID, COLUMN_ADDRESS, COLUMN_LATITUDE, COLUMN_LONGITUDE,
                            COLUMN_ATTRIBUTIONS, COLUMN_IMAGE),
                    "$COLUMN_ID=?",
                    arrayOf(id.toString()),
                    null,
                    null,
                    null,
                    null)

            if (cursor != null) {
                cursor.moveToFirst()
                val place = ParcelablePlace(
                        cursor.getString(cursor.getColumnIndex(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)),
                        LatLng())
            }
        }

        // Parcelable
        override fun createFromParcel(parcel: Parcel): ParcelablePlace {
            return ParcelablePlace(parcel)
        }

        override fun newArray(size: Int): Array<ParcelablePlace?> {
            return arrayOfNulls(size)
        }

        fun fromPlace(place: Place): ParcelablePlace {
            var locale = place.locale
            if (locale == null) {
                locale = Locale.getDefault()
            }

            var uri = place.websiteUri
            if (uri == null) {
                uri = Uri.parse("")
            }

            var attr = place.attributions
            if (attr == null) {
                attr = ""
            }

            return ParcelablePlace(place.id,
                    place.name.toString(),
                    place.address.toString(),
                    place.latLng,
                    locale,
                    uri,
                    attr.toString(),
                    null)
        }
    }

    private val GEOFENCE_EXPIRATION_IN_HOURS = 1L
    private val GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000
    val GEOFENCE_RADIUS_IN_METERS = 100.0f

    private var databaseId : Int = -1

    private constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readParcelable<LatLng>(LatLng::class.java.classLoader),
            LocaleHelper.stringToLocale(parcel.readString()),
            Uri.parse(parcel.readString()),
            parcel.readString(),
            parcel.readParcelable(Bitmap::class.java.classLoader)
    )

    // SQLite
    fun getContentValues(): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(COLUMN_ID, id)
        contentValues.put(COLUMN_NAME, name)
        contentValues.put(COLUMN_ADDRESS, address)
        contentValues.put(COLUMN_LATITUDE, latLng.latitude)
        contentValues.put(COLUMN_LONGITUDE, latLng.longitude)
        contentValues.put(COLUMN_WEBSITE, website.toString())
        contentValues.put(COLUMN_ATTRIBUTIONS, attributions)

        val bitmap = mImage
        if (bitmap != null) {
            contentValues.put(COLUMN_IMAGE, DatabaseBitmapUtility.getBytes(bitmap))
        }

        return contentValues
    }

    /**
     * Build a Geofence object for this place
     */
    fun getGeofence(): Geofence {
        return Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(id)

                // Set the circular region of this geofence.
                // Recommended value is a 100 meters for most situations. If the geofence is in the
                // countryside, 500 meters could be used.
                .setCircularRegion(latLng.latitude,
                        latLng.longitude,
                        GEOFENCE_RADIUS_IN_METERS
                )
                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                // Set to 12 hours for now, but this value could be decreased since most travels
                // don't take that long.
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(address)
        parcel.writeParcelable(latLng, 0)
        parcel.writeString(LocaleHelper.localeToString(locale))
        parcel.writeString(website.toString())
        parcel.writeString(attributions)
        parcel.writeValue(mImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getImage(): Bitmap? {
        if (mImage != null) {
            return mImage
        }

//        TODO
//        val image = cursor.getBlob(1)
        return mImage
    }

    fun setImage(bitmap: Bitmap) {
        mImage = bitmap
    }
}