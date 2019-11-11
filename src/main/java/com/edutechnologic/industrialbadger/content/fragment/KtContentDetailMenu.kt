package com.edutechnologic.industrialbadger.content.fragment

import androidx.fragment.app.Fragment

class KtContentDetailMenu : Fragment() {
    companion object {
        val ARG_IS_OPEN = "com.industrialbadger.content.menu.IS_OPEN"
    }
    var open: Boolean = false
        private set(value) {
            field = value
        }
}