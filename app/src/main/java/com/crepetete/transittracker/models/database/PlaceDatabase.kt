package com.crepetete.transittracker.models.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.crepetete.transittracker.models.place.PlaceData

/**
 *
 * Created by Patrick van de Graaf on 5/18/2018.
 *
 */
@Database(entities = [(PlaceData::class)], version = 1)
abstract class PlaceDatabase : RoomDatabase() {
    abstract fun placeDataDao(): PlaceDataDao

    companion object {
        private var INSTANCE: PlaceDatabase? = null
        private const val DATABASE_NAME = "places.db"

        fun getInstance(context: Context): PlaceDatabase? {
            if (INSTANCE == null) {
                synchronized(PlaceDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            PlaceDatabase::class.java, DATABASE_NAME)
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}