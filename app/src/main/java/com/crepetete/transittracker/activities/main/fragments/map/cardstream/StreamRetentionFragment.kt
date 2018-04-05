package com.crepetete.transittracker.activities.main.fragments.map.cardstream

import android.os.Bundle
import android.support.v4.app.Fragment

class StreamRetentionFragment : Fragment() {
    private var mState: CardStreamState? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        retainInstance = true
    }

    fun storeCardStream(state: CardStreamState) {
        mState = state
    }

    fun getCardStream(): CardStreamState? {
        return mState
    }
}