package com.crepetete.transittracker.views.fragments.settings


import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import com.crepetete.transittracker.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
