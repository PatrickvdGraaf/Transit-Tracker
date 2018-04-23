package com.crepetete.transittracker.config

import android.app.Activity
import android.support.annotation.IdRes
import android.view.View

/**
 * Helper class that makes it possible to lazily load views by Id as a non-mutable (val)
 * property.
 *
 * @return Lazy<T>, which ensures that the returned property won't be initialized right away but the
 * first time the value is actually needed.
 */
fun <T : View> Activity.bind(@IdRes idRes: Int): Lazy<T> {
    return unsafeLazy { findViewById<T>(idRes) }
}

private fun <T> unsafeLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)