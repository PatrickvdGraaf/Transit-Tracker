package com.crepetete.transittracker.config.locale

import android.content.Context
import android.os.Build
import timber.log.Timber
import java.util.*

class LocaleHelper {
    companion object {
        private const val DELIMITER = ","

        fun localeToString(l: Locale): String {
            return "${l.language}$DELIMITER${l.country}"
        }

        /**
         * Creates a [Locale] from a String.
         *
         * We overload this method so the user can use this when he has certainty that the string
         * is correctly formatted.
         * Just in case, we pass the JVMs default Locale as a fallback.
         *
         * @param s String that was formatted via the [localeToString].
         * @return Locale
         */
        fun stringToLocale(s: String): Locale {
            return stringToLocale(s, Locale.getDefault())
        }

        /**
         * Creates a [Locale] from a String.
         *
         * Overloads so we can get the users Locale based on the context.
         * This provides a better fallback.
         *
         * @param s String that was formatted via the [localeToString].
         * @param context For getting the users Locale.
         * @return Locale
         */
        fun stringToLocale(s: String, context: Context): Locale {
            val userLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales.get(0)
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale
            }
            return stringToLocale(s, userLocale)
        }


        /**
         * Does the actual work.
         *
         * Dissects the given string using the [DELIMITER].
         * It then tries to make a Locale from the extracted language and country stored in val l
         * and c.
         *
         * @param s String that was formatted via the [localeToString].
         * @param fallback Locale to use when something goes wrong during extracting or parsing
         *                 the values of string s.
         * @return Locale
         */
        private fun stringToLocale(s: String, fallback: Locale): Locale {
            try {
                val tempStringTokenizer = StringTokenizer(s, DELIMITER)
                var l: Any? = null
                if (tempStringTokenizer.hasMoreTokens()) {
                    l = tempStringTokenizer.nextElement()
                }
                var c: String? = null
                if (tempStringTokenizer.hasMoreTokens()) {
                    c = tempStringTokenizer.nextElement() as String?
                }

                if (l != null && c != null) {
                    return Locale(l as String?, c as String?)
                }
            } catch (e: Exception) {
                Timber.d("Failed to parse a Locale with string '$s'.")
            }
            return fallback
        }
    }
}