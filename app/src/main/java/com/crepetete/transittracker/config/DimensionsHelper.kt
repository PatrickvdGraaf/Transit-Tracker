package com.crepetete.transittracker.config

import android.content.Context
import android.util.DisplayMetrics

class DimensionsHelper {
    companion object {
        fun pxToDp(context: Context, px: Int): Int {
            val displayMetrics = context.resources.displayMetrics
            return Math.round((px / (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)).toDouble()).toInt()
        }
    }
}