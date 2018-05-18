package com.crepetete.transittracker.models.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.crepetete.transittracker.models.place.ParcelablePlace

/**
 *
 * Created by Patrick van de Graaf on 5/18/2018.
 *
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, TABLE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "personal_stops"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(ParcelablePlace.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed
        db?.execSQL("DROP TABLE IF EXISTS " + ParcelablePlace.TABLE_NAME)

        // Create tables again
        onCreate(db)
    }

    // Places
    fun insertPlace(place: ParcelablePlace): Long {
        val database = writableDatabase
        val contentValues = place.getContentValues()

        val id = database.insert(ParcelablePlace.TABLE_NAME, null, contentValues)
        database.close()

        return id
    }

    fun getPlace(databaseId: Long): ParcelablePlace? {
        val database = readableDatabase

        val cursor = ParcelablePlace.getCursor(database, databaseId)

        if (cursor != null) {
            cursor.moveToFirst()
            val place = ParcelablePlace(
                    cursor.getString()
            )
        }
    }
}