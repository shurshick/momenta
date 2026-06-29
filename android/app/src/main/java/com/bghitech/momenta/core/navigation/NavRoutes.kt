package com.bghitech.momenta.core.navigation

import android.net.Uri

object NavRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val AUTH = "auth"
    const val AUTH_LOGIN = "auth/login"
    const val AUTH_REGISTER = "auth/register"
    const val MAIN = "main"
    const val MAIN_FEED = "main_feed"
    const val TODAY = "today"
    const val SEARCH = "search"
    const val FEED = "feed"
    const val CIRCLE = "circle"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val CAMERA = "camera"
    const val PREVIEW = "preview/{imagePath}"
    const val UPLOAD_SUCCESS = "upload_success"

    fun preview(imagePath: String) = "preview/${Uri.encode(imagePath)}"
}
