package com.crepetete.transittracker.models.place

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.crepetete.transittracker.config.locale.LocaleHelper
import com.crepetete.transittracker.models.database.DatabaseBitmapUtility
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLng
import java.util.*

class ParcelablePlace @SuppressLint("ValidFragment") private constructor(
        internal val id: String,
        internal val name: String,
        internal val address: String,
        internal val latLng: LatLng,
        val locale: Locale,
        val website: Uri?,
        val attributions: String,
        private var mImage: Bitmap?) : Parcelable {

    constructor(data: PlaceData) : this(data.id,
            data.name,
            data.address,
            LatLng(data.latitude, data.longitude),
            LocaleHelper.stringToLocale(data.locale),
            Uri.parse(data.website),
            data.attributions,
            DatabaseBitmapUtility.getImage(data.image)
    )

    companion object CREATOR : Parcelable.Creator<ParcelablePlace> {
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
        return mImage
    }

    fun setImage(bitmap: Bitmap) {
        mImage = bitmap
    }
}