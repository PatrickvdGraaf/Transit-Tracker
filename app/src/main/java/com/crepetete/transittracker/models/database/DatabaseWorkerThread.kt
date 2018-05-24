package com.crepetete.transittracker.models.database

import android.os.Handler
import android.os.HandlerThread

/**
 *
 * Created by Patrick van de Graaf on 5/18/2018.
 *
 */
class DatabaseWorkerThread() : HandlerThread("databaseWorkerThread") {
    private lateinit var mWorkerHandler: Handler

    override fun onLooperPrepared() {
        super.onLooperPrepared()
        mWorkerHandler = Handler(looper)
    }

    fun postTask(task: Runnable) {
        if (looper != null) {
            mWorkerHandler = Handler(looper)
        }
        mWorkerHandler.post(task)
    }
}