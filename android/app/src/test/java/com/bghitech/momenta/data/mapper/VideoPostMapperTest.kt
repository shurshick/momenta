package com.bghitech.momenta.data.mapper

import com.bghitech.momenta.data.remote.dto.PostDto
import org.junit.Assert.assertEquals
import org.junit.Test

class VideoPostMapperTest {

    @Test
    fun `video original url survives cache round trip`() {
        val originalUrl = "https://media.test/original.mp4"
        val cachedPost = PostDto(
            id = "video-post",
            mediaType = "video",
            originalUrl = originalUrl,
            previewUrl = "https://media.test/preview.webp",
            challengeDate = "2026-08-02"
        ).toDomain().toCachedEntity(accountId = "account")

        val restoredPost = cachedPost.toDomain()

        assertEquals("video", restoredPost.mediaType)
        assertEquals(originalUrl, restoredPost.originalUrl)
    }
}
