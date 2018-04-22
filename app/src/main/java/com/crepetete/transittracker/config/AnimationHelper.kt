package com.crepetete.transittracker.config

class AnimationHelper {
    companion object {
        /**
         * The length of ensuing property animations, in milliseconds.
         *
         * Animations slower than 400 milliseconds might feel slow.
         * As per the <a href="https://material.io/guidelines/motion/duration-easing.html#duration-easing-common-durations">Material Guidelines</a>
         */
        const val FAST = 50L
        const val QUICK = 200L
        const val NORMAL = 300L
        const val SLOW = 375L
    }
}