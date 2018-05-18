package com.crepetete.transittracker.models.view.preference

import android.os.Bundle
import android.support.v7.preference.PreferenceDialogFragmentCompat
import android.view.View
import android.widget.SeekBar
import com.crepetete.transittracker.R

class SeekBarPreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {
    companion object {
        private const val ARG_KEY = "ARG_KEY"

        fun getInstance(key: String): SeekBarPreferenceDialogFragmentCompat {
            val fragment = SeekBarPreferenceDialogFragmentCompat()
            val bundle = Bundle(1)
            bundle.putString(ARG_KEY, key)
            fragment.arguments = bundle

            return fragment
        }
    }

    private lateinit var mSeekBar: SeekBar

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)

        mSeekBar = view?.findViewById<SeekBar>(R.id.seekbar)
                ?: throw IllegalStateException("SeekBarPreferenceDialogFragmentCompat must " +
                "contain a SeekBar.")
        val preference = preference
        if (preference is SeekBarPreference) {
            val radius = preference.getRadius()
            mSeekBar.progress = radius
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val preference = preference
            if (preference is SeekBarPreference) {
                val radius = mSeekBar.progress
                if (preference.callChangeListener(radius)) {
                    preference.setRadius(radius)
                }
            }
        }
    }
}