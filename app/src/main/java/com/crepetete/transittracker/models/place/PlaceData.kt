package com.crepetete.transittracker.models.place

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import com.crepetete.transittracker.config.locale.LocaleHelper
import com.crepetete.transittracker.models.database.DatabaseBitmapUtility
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLng
import java.util.*

/**
 *
 * Created by Patrick van de Graaf on 5/18/2018.
 *
 */
@Entity(tableName = "places")
data class PlaceData(@PrimaryKey(autoGenerate = true)
                     @ColumnInfo(name = "uid") var uid: Long? = null,
                     @ColumnInfo(name = "id") var id: String = "",
                     @ColumnInfo(name = "name") var name: String = "",
                     @ColumnInfo(name = "address") var address: String = "",
                     @ColumnInfo(name = "latitude") var latitude: Double = .0,
                     @ColumnInfo(name = "longitude") var longitude: Double = .0,
                     @ColumnInfo(name = "locale") var locale: String = BASE_LOCALE,
                     @ColumnInfo(name = "website") var website: String = "",
                     @ColumnInfo(name = "attributions") var attributions: String = "",
                     @ColumnInfo(name = "image", typeAffinity = ColumnInfo.BLOB)
                     var image: ByteArray = ByteArray(0)) : Parcelable {

    constructor(place: Place) : this(null, place.id, place.name.toString(),
            place.address.toString(), place.latLng.latitude, place.latLng.longitude,
            if (place.locale != null) {
                LocaleHelper.localeToString(place.locale)
            } else {
                BASE_LOCALE
            }, place.websiteUri.toString(),
            place.attributions.toString())

    constructor(parcel: Parcel) : this(
            parcel.readValue(Long::class.java.classLoader) as? Long,
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.createByteArray()) {
    }

    @Ignore
    constructor() : this(null, "", "", "", .0, .0, "",
            "", "", ByteArray(0))

    fun getLatLng(): LatLng = LatLng(latitude, longitude)

    fun setBitmap(bitmap: Bitmap) {
        val byteArray = DatabaseBitmapUtility.getBytes(bitmap)
        if (byteArray != null) {
            image = byteArray
        }
    }

    fun getBitmap(): Bitmap? {
        if (!image.isEmpty()) {
            return DatabaseBitmapUtility.getImage(image)
        }
        return null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaceData

        if (uid != other.uid) return false
        if (id != other.id) return false
        if (name != other.name) return false
        if (address != other.address) return false
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (locale != other.locale) return false
        if (website != other.website) return false
        if (attributions != other.attributions) return false
        if (!Arrays.equals(image, other.image)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uid?.hashCode() ?: 0
        result = 31 * result + id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + locale.hashCode()
        result = 31 * result + website.hashCode()
        result = 31 * result + attributions.hashCode()
        result = 31 * result + Arrays.hashCode(image)
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(uid)
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(address)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(locale)
        parcel.writeString(website)
        parcel.writeString(attributions)
        parcel.writeByteArray(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlaceData> {
        private const val BASE_LOCALE = "en_GB"
        override fun createFromParcel(parcel: Parcel): PlaceData {
            return PlaceData(parcel)
        }

        override fun newArray(size: Int): Array<PlaceData?> {
            return arrayOfNulls(size)
        }
    }
}