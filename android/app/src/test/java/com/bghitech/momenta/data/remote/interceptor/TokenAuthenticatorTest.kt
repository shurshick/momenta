package com.bghitech.momenta.data.remote.interceptor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenAuthenticatorTest {
    @Test
    fun invalidRefreshResponseClearsSession() {
        assertTrue(shouldClearSessionAfterRefresh(400))
        assertTrue(shouldClearSessionAfterRefresh(401))
        assertTrue(shouldClearSessionAfterRefresh(403))
    }

    @Test
    fun transientRefreshResponseKeepsSession() {
        assertFalse(shouldClearSessionAfterRefresh(429))
        assertFalse(shouldClearSessionAfterRefresh(500))
        assertFalse(shouldClearSessionAfterRefresh(503))
    }
}
