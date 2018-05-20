package com.crepetete.transittracker.models.place

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
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
                     @ColumnInfo(name = "locale") var locale: String = "en_GB",
                     @ColumnInfo(name = "website") var website: String = "",
                     @ColumnInfo(name = "attributions") var attributions: String = "",
                     @ColumnInfo(name = "image", typeAffinity = ColumnInfo.BLOB)
                     var image: ByteArray = ByteArray(0)) {

    @Ignore
    constructor() : this(null, "", "", "", .0, .0, "",
            "", "", ByteArray(0))

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
}