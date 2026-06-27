package com.bghitech.momenta.core.datastore

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenProvider @Inject constructor() {
    @Volatile
    private var accessToken: String? = null

    @Volatile
    private var refreshToken: String? = null

    fun accessToken(): String? = accessToken

    fun refreshToken(): String? = refreshToken

    fun update(accessToken: String?, refreshToken: String?) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    fun clear() {
        accessToken = null
        refreshToken = null
    }
}
