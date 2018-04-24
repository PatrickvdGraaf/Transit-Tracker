package com.crepetete.transittracker.models.place

interface PlacesListener {
    fun getListenerTag(): String
    fun onPlacesChanged(updatedPosition: Int)
    fun onPlaceRemoved(removedPosition: Int)
}