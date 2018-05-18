package com.crepetete.transittracker.models.view.preference

import android.content.Context
import android.content.res.TypedArray
import android.support.annotation.LayoutRes
import android.support.v7.preference.DialogPreference
import android.util.AttributeSet
import com.crepetete.transittracker.R


class SeekBarPreference @JvmOverloads constructor(context: Context,
                                                  attrs: AttributeSet? = null,
                                                  defStyleAttr: Int = R.attr.dialogPreferenceStyle,
                                                  defStyleRes: Int)
    : DialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    @LayoutRes
    private val mDialogLayoutResId = R.layout.seekbar_view_layout
    private var mRadius = 500

    fun getRadius(): Int {
        return mRadius
    }

    fun setRadius(newRadius: Int) {
        mRadius = newRadius

        persistInt(newRadius)
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a?.getInt(index, mRadius) ?: super.onGetDefaultValue(a, index)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (restorePersistedValue) {
            setRadius(getPersistedInt(mRadius))
        } else {
            setRadius(defaultValue as Int)
        }
    }

    override fun getDialogLayoutResource(): Int {
        return mDialogLayoutResId
    }
}