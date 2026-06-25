package com.bghitech.momenta.core.navigation

object NavRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val AUTH = "auth"
    const val MAIN = "main"
    const val TODAY = "today"
    const val SEARCH = "search"
    const val FEED = "feed"
    const val CIRCLE = "circle"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val CAMERA = "camera"
    const val PREVIEW = "preview/{imagePath}"
    const val UPLOAD_SUCCESS = "upload_success"

    fun preview(imagePath: String) = "preview/$imagePath"
}
