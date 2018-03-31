package com.crepetete.transittracker.activities.main

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.crepetete.transittracker.R
import com.crepetete.transittracker.activities.main.fragments.HomeFragment
import com.crepetete.transittracker.activities.main.fragments.MapsFragment
import com.crepetete.transittracker.activities.main.fragments.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var mFragment: Fragment? = null
    private val mFragmentManager = supportFragmentManager

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                mFragment = HomeFragment()
            }
            R.id.navigation_dashboard -> {
                mFragment = MapsFragment()
            }
            R.id.navigation_notifications -> {
                mFragment = SettingsFragment()
            }
        }
        val transaction = mFragmentManager.beginTransaction()
        transaction.replace(R.id.container, mFragment).commit()
        return@OnNavigationItemSelectedListener true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

    }
}
