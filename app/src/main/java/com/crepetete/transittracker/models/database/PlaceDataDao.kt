package com.crepetete.transittracker.models.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import com.crepetete.transittracker.models.place.PlaceData

/**
 *
 * Created by Patrick van de Graaf on 5/18/2018.
 *
 */
@Dao
interface PlaceDataDao {
    @Query("SELECT * from places")
    fun getAll(): List<PlaceData>

    @Insert(onConflict = REPLACE)
    fun insert(placeData: PlaceData)

    @Query("DELETE from places WHERE id IS :placeId")
    fun delete(placeId: String)

    @Query("DELETE from places")
    fun deleteAll()
}