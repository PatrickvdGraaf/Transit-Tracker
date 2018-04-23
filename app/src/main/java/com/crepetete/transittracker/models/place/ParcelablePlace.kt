package com.crepetete.transittracker.models.place

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.crepetete.transittracker.config.locale.LocaleHelper
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import java.util.*


class ParcelablePlace @SuppressLint("ValidFragment") private constructor(
        internal val id: String,
        internal val address: String,
        private val locale: Locale,
        internal val name: String,
        internal val latLng: LatLng,
        private val viewPort: LatLngBounds?,
        private val website: Uri?,
        private val attributions: String) : Parcelable {

    private lateinit var mPhotoResult: Bitmap

    companion object CREATOR : Parcelable.Creator<ParcelablePlace> {
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

            return ParcelablePlace(place.id, place.address.toString(), locale,
                    place.name.toString(), place.latLng, place.viewport, uri,
                    attr.toString())
        }
    }

    private constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            LocaleHelper.stringToLocale(parcel.readString()),
            parcel.readString(),
            parcel.readParcelable<LatLng>(LatLng::class.java.classLoader),
            parcel.readParcelable<LatLngBounds>(LatLngBounds::class.java.classLoader),
            Uri.parse(parcel.readString()),
            parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(address)
        parcel.writeString(LocaleHelper.localeToString(locale))
        parcel.writeString(name)
        parcel.writeParcelable(latLng, 0)
        parcel.writeParcelable(viewPort, 0)
        parcel.writeString(website.toString())
        parcel.writeString(attributions)
    }

    override fun describeContents(): Int {
        return 0
    }
}